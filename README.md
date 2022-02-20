This repository contains tools to build polyglot ASTs from source code of GraalVM applications. It currently partially supports python and javascript, and is built in a manner that aims to facilitate addition of new languages.  
You will need JSitter in order to be able to run the project, which is available at https://github.com/JetBrains/jsitter  

There are two main classes to be looking at.

# PolyglotTreeHandler

This class mainly acts as a wrapper for a tree-sitter object, that has methods to help handle polyglot-specific functions. 

The main functions to look at are `makePolyglotSubtree`, `isPolyglotEvalCall`, `isPolyglotImportCall` and `isPolyglotExportCall` as they are the ones most closely linked to polyglot features. 

`makePolyglotSubtree` handles the building and linking of a tree from a foreign code evaluation. It registers the code that would be executed, builds a subsequent polyglot tree handler to wrap it, and adds it to the list of linked nodes to allow traversing.

The other three functions mark nodes that are related to polyglot by returning a boolean. They are mainly a public interface for users of the framework, but require extension whenever a language is added.

# PolyglotZipper

This class acts as a wrapper for tree-sitter zipper object, which allows navigation of an AST through the `up`, `left`, `right` and `down` primitives. Those calls are reimplemented to allow similar usage of a polyglot AST without having to significantly acknowledge the polyglot nature of the AST, by leveraging the built polyglot tree handler it operates on; in addition, polyglot zippers provide functions to mark polyglot related nodes as such.

# Other classes

The other classes in the project give examples of services that could be implemented onto this framework in order to provide code analysis features. They are essentially dummy use-cases to demonstrate how to make use of the two main classes.

The repository also includes an App class with a runnable main function which gives another short example of how to use the framework.