# Your Program Is a Language - Examples

## Calculator
Found in [`examples/calculator`](examples/calculator/src)

This example shows a kind of DSL that is relatively simple to implement in as both an ADT as well as a "tagless final" style algebra.
Both approaches allow for the same code to be written, but they are encoded in slightly different ways.

The ADT approach requires some kind of data structure to work, so we have a compiler for a `List[CalculationOp]` that runs our sequence of operations.
The tagless approach, on the other hand, implements each case class with a function on a trait.
Our tagless version contains an interpreter

## CRUDLang
Found in [`examples/crudlang`](examples/crudlang/src)

This is a more particular example of a DSL being used to solve a business problem.
We want to spin up a bunch of CRUD applications relatively quickly, so instead of doing a lot of the wiring from scratch, we build up a little DSL to express how we want it to work.
