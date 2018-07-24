package parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * A Tokenizer turns a Reader into a stream of tokens that can be iterated over
 * using a {@code for} loop.
 */
public class Tokenizer implements Iterator<Token> {

    /**
     * BufferedReader to read from the {@code Reader} provided in the
     * constructor, not {@code null}.
     */
  private final BufferedReader br;
  
  /**
   * Builder to store read characters.
   */
  private final StringBuilder sb;
  
    /**
     * The number of the line being parsed from the reader. Starts at 1 and
     * always equals 1 + the number of new line characters encountered.
     */
  private int lineNo;
  
  /**
   * {@code tokenReady} is {@code false} if a token is not immediately
   * available to be returned from {@code next()}, and {@code true} if a token
   * is immediately ready to be returned from {@code next()}.
   */
  private boolean tokenReady;
  
  /**
   * The most recent token processed by this Tokenizer, or an error token.
   * Not {@code null}.
   */
  private Token curTok;
  
  /**
   * {@code false} if the EOF has not been encountered,
   * {@code true} if it has been encountered
   */
  private boolean atEOF;

  /**
   * Create a Tokenizer that reads from the specified reader
   *
   * @param r
   *            The source from which the Tokenizer lexes input into Tokens
   */
  public Tokenizer(Reader r) {
    br = new BufferedReader(r);
    sb = new StringBuilder();
    lineNo = 1;
    tokenReady = false;
        curTok = new Token.ErrorToken("Tokenizer has not yet begun reading");
        atEOF = false;
  }

  /**
   * Returns {@code true} if the iteration has more meaningful elements. (In
   * other words, returns {@code true} if {@link #next} would return a non-EOF
   * element rather than throwing an exception or returning EOF.)
   *
   * @return {@code true} if the iteration has more meaningful elements
   */
  @Override
  public boolean hasNext() {
    if (!tokenReady) {
      try { lexOneToken(); }
      catch (IOException e) { throw new TokenizerIOException(e); }
      catch (EOFException e) { return false; }
    }
    return true;
  }

  @Override
  public Token next() throws TokenizerIOException {
    Token tok = peek();
    tokenReady = false;
    return tok;
  }

  /**
   * Return the next token in the program without consuming the token.
   *
   * @return the next token, without consuming it
   * @throws IOException
   *             if an IOException was thrown while trying to read from the
   *             source Reader
   * @throws EOFException
   *             if EOF was encountered while trying to lex the next token
   */
  public Token peek() throws TokenizerIOException {
    if (!tokenReady && !atEOF) {
      try { lexOneToken(); }
      catch (IOException e) { throw new TokenizerIOException(e); }
      catch (EOFException e) {
        // EOFException is thrown by encounteredEOF(), which should set
        // curTok to an EOFToken, so this catch block should be empty.
      }
    }
    return curTok;
  }
 
  
  @Override
  public void remove() {
    tokenReady = false;
  }

  /**
   * Close the reader opened by this tokenizer.
   */
  void close() {
    try { br.close(); }
    catch (IOException e) {
      System.out.println("IOException:");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Read one token from the reader. One token is always produced if the end
   * of file is not encountered, but that token may be an error token.
   *
   * @throws IOException
   *             if an IOException was thrown when trying to read from the
   *             source Reader
   * @throws EOFException
   *             if EOF is encountered and a token cannot be produced.
   */
  private void lexOneToken() throws IOException, EOFException {
    setBuilderToFirstMeaningfulChar();
    char c = sb.charAt(0);

    switch (c) {
    case '[':
      setNextTokenAndReset(TokenType.LBRACKET);
      break;
    case ']':
      setNextTokenAndReset(TokenType.RBRACKET);
      break;
    case '(':
      setNextTokenAndReset(TokenType.LPAREN);
      break;
    case ')':
      setNextTokenAndReset(TokenType.RPAREN);
      break;
    case '{':
      setNextTokenAndReset(TokenType.LBRACE);
      break;
    case '}':
      setNextTokenAndReset(TokenType.RBRACE);
      break;
    case ';':
      setNextTokenAndReset(TokenType.SEMICOLON);
      break;
    case '=':
      setNextTokenAndReset(TokenType.EQ);
      break;
    case '+':
      setNextTokenAndReset(TokenType.PLUS);
      break;
    case '*':
      setNextTokenAndReset(TokenType.MUL);
      break;
    case '/':
      setNextTokenAndReset(TokenType.DIV);
      break;
    case '<':
      lexLAngle();
      break;
    case '>':
      lexRAngle();
      break;
    case '-':
      lexDash();
      break;
    case ':':
      if (consume('=')) { setNextTokenAndReset(TokenType.ASSIGN); }
      break;
    case '!':
      if (consume('=')) { setNextTokenAndReset(TokenType.NE); }
      break;
    default:
      if (Character.isLetter(c)) { lexIdentifier(); }
      else if (Character.isDigit(c)) { lexNum(); }
      else { unexpected(); }
    }
  }

  /**
   * Consumes whitespace up until the first non-whitespace character, and sets
   * the builder to that character
   *
   * @throws IOException
   *             if an IOException is encountered while reading from the
   *             source Reader
   * @throws EOFException
   *             if EOF is encountered and a token cannot be produced.
   */
    private void setBuilderToFirstMeaningfulChar()
            throws IOException, EOFException {
    // Make sure there isn't any leftover from a previous lexing operation
    assert sb.length() <= 1;
    char c = sb.length() == 1 ? c = sb.charAt(0) : getNextCharAndAppend();

    // consume whitespace
    while (Character.isWhitespace(c)) {
      if (c == '\n') { ++lineNo; }
      c = getNextCharAndAppend();
    }
    
    if(c == '/') {
    	c = (char) nextChar(true);
    	if(c == '/') {
    		br.readLine();
    		c = getNextCharAndAppend();
    		resetBuilderWith(c);
    	}
    	else {
    		resetBuilderWith('/');
    		sb.append(c);
    	}
    }
    else {
    	resetBuilderWith(c);
    }

  }
  
  /**
     * Lexes a left angle bracket. May be called only when the previously read
     * character is '<'.
     * 
     * @throws IOException
     *             if an IOException was thrown when trying to read from the
     *             source Reader
     * @throws EOFException
     *             if EOF is encountered and a token cannot be produced.
     */
  private void lexLAngle() throws IOException, EOFException {
    int c = nextChar(false);
    if (c == -1) { setNextTokenAndReset(TokenType.LT); }
    else {
      char cc = (char) c;
      sb.append(cc);
      if (cc == '=') { setNextTokenAndReset(TokenType.LE); }
      else { setNextTokenAndResetWith(TokenType.LT, cc); }
    }
  }

  /**
     * Lexes a right angle bracket. May be called only when the previously read
     * character '>'.
     * 
     * @throws IOException
     *             if an IOException was thrown when trying to read from the
     *             source Reader
     * @throws EOFException
     *             if EOF is encountered and a token cannot be produced.
     */
  private void lexRAngle() throws IOException, EOFException {
    int c = nextChar(false);
    if (c == -1) { setNextTokenAndReset(TokenType.GT); }
    else {
      char cc = (char) c;
      sb.append(cc);
      if (cc == '=') { setNextTokenAndReset(TokenType.GE); }
      else { setNextTokenAndResetWith(TokenType.GT, cc); }
    }
  }

  /**
     * Lexes a dash character. If a dash is followed by another dash, then it is
     * part of an arrow. Otherwise it must represent a minus sign.
     * 
     * May only be called when the previously read char is a dash '-'.
     * 
     * @throws IOException
     *             if an IOException was thrown when trying to read from the
     *             source Reader
     * @throws EOFException
     *             if EOF is encountered and a token cannot be produced.
     */
  private void lexDash() throws IOException, EOFException {
    int c = nextChar(false);
    if (c == -1) { setNextTokenAndReset(TokenType.MINUS); }
    else {
      char cc = (char) c;
      sb.append(cc);
      if (cc == '-') {
        if (consume('>')) { setNextTokenAndReset(TokenType.ARR); }
      } else {
        setNextTokenAndResetWith(TokenType.MINUS, cc);
      }
    }
  }

  /**
     * Lexes an identifier. May be called only when the previously read
     * character is a letter.
     * 
     * @throws IOException
     *             if an IOException was thrown when trying to read from the
     *             source Reader
     * @throws EOFException
     *             if EOF is encountered and a token cannot be produced.
     */
  private void lexIdentifier() throws IOException, EOFException {
    int c;
    for (c = nextChar(false); c != -1 && Character.isLetter(c);
            c = nextChar(false)) {
      sb.append((char) c);
    }

    String id = sb.toString();
    TokenType tt = TokenType.getTypeFromString(id);
    if (tt != null) { setNextTokenAndReset(tt); }
    else { unexpected(); }

    if (c != -1) { sb.append((char) c); }
  }

  /**
     * Lexes a number. May be called only when the previously read
     * character is a digit.
     * 
     * @throws IOException
     *             if an IOException was thrown when trying to read from the
     *             source Reader
     * @throws EOFException
     *             if EOF is encountered and a token cannot be produced.
     */
  private void lexNum() throws IOException, EOFException {
    int c;
    for (c = nextChar(false); c != -1 && Character.isDigit(c);
            c = nextChar(false)) {
      sb.append((char) c);
    }

    try {
      String num = sb.toString();
      int val = Integer.parseInt(num);
      curTok = new Token.NumToken(val, lineNo);
      tokenReady = true;
      resetStringBuilder();
      if (c != -1) { sb.append((char) c); }
    } catch (NumberFormatException e) { unexpected(); }
  }

  /**
   * Read the next character from the reader, treating EOF as an error. If
   * successful, append the character to the buffer.
   *
   * @return The next character
   * @throws IOException
   *             if an IOException was thrown when trying to read the next
   *             char
   * @throws EOFException
   *             if EOF is encountered
   */
  private char getNextCharAndAppend() throws IOException, EOFException {
    char c = (char) nextChar(true);
    sb.append(c);
    return c;
  }

  /**
     * Read the next character from the reader. If {@code exceptionOnEOF}, treat
     * EOF as an error. If successful, append the character to the buffer.
     *
     * @param exceptionOnEOF
     *            {@code true} if EOF should be treated as an error
     * @return The integer representation of the next character
     * @throws IOException
     *             if an {@code IOException} is thrown when trying to read from
     *             the source Reader
     * @throws EOFException
     *             if EOF is encountered and isEOFerror is true
     */
    private int nextChar(boolean exceptionOnEOF)
            throws IOException, EOFException {
    int c = br.read(); // returns -1 if the stream's end has been reached
    if (exceptionOnEOF && c == -1) { encounteredEOF(); }
    return c;
  }

  /**
     * Sets the next token to be a token of {@code tokenType} and clears the
     * {@code StringBuilder}.
     * 
     * @param tokenType
     *            the type of the token to set, not {@code null}
     */
  private void setNextTokenAndReset(TokenType tokenType) {
    curTok = new Token(tokenType, lineNo);
    tokenReady = true;
    resetStringBuilder();
  }

  /**
     * Sets the next token to be a token of {@code tokenType}, clears the
     * {@code StringBuilder}, and inserts {@code c} to begin the next string.
     * 
     * @param tokenType
     *            the type of the token to set, not {@code null}
     * @param c
     *            the character to use at the start of the string builder
     */
  private void setNextTokenAndResetWith(TokenType tokenType, char c) {
    setNextTokenAndReset(tokenType);
    sb.append(c);
  }

  /**
     * Resets the StringBuilder and starts a new string with {@code c}.
     * 
     * @param c
     *            the character with which to start a new string
     */
  private void resetBuilderWith(char c) {
      resetStringBuilder();
    sb.append(c);
  }
  
  /**
   * Resets the StringBuilder {@code sb} to clear its string.
   */
  private void resetStringBuilder() {
      sb.setLength(0);
  }

  /**
   * Read the next character and determine whether it is the expected
   * character. If not, the current buffer is an error
   *
   * @param expected
   *            The expected next character
   * @return true if the next character is as expected
   * @throws IOException
   *             if an IOException was thrown when trying to read from the
   *             source Reader
   * @throws EOFException
   *             if EOF is encountered
   */
  private boolean consume(char expected) throws IOException, EOFException {
    int c = getNextCharAndAppend();
    if (c == expected) { return true; }
    unexpected();
    return false;
  }

  /**
   * Makes the current token an error token with the current contents of the
   * buffer.
   */
  private void unexpected() {
    curTok = new Token.ErrorToken(sb.toString());
    tokenReady = true;
    resetStringBuilder();
  }

  /**
   * Make the contents of the current buffer into an EOFToken, clearing the
   * buffer in the process, set atEOF to true, and set the current token to
   * the newly generated EOFToken, setting tokenReady in the process
   */
  private void encounteredEOF() throws EOFException {
    curTok = new Token.EOFToken(sb.toString(), lineNo);
    resetStringBuilder();
    atEOF = true;
    tokenReady = true;
    throw new EOFException();
  }

  /**
   * "Helper" exception to indicate that EOF was reached
   */
  static class EOFException extends Exception {
        /** Unique serial version ID. @see Serializable#serialVersionUID */
    private static final long serialVersionUID = -7333947165525391472L;
  }

  /**
   * "Helper" exception to indicate an IO exception while tokenizing.
   */
  static class TokenizerIOException extends RuntimeException {
      /** Unique serial version ID. @see Serializable#serialVersionUID */
    private static final long serialVersionUID = 8014027094822746940L;

    /**
         * Constructs a new {@code TokenizerIOException} caused by
         * {@code cause}.
         * 
         * @param cause
         *            the cause of the IOException
         */
    TokenizerIOException(Throwable cause) {
      super(cause);
    }
  }
}
