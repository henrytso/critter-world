package ast;

/**
 * A factory that produces the public static Mutation objects corresponding to
 * each mutation
 */
public class MutationFactory {
   public static Mutation getRemove() {
      return new RemoveMutation();
   }

   public static Mutation getSwap() {
      return new SwapMutation();
   }

   public static Mutation getReplace() {
	   return new ReplaceMutation();
   }

   public static Mutation getTransform() {
	   return new TransformMutation();
   }

   public static Mutation getInsert() {
	   return new InsertMutation();
   }

   public static Mutation getDuplicate() {
	   return new DuplicateMutation();
   }
}
