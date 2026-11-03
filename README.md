# analyzer-compat-demo

Source and binary compatibility of the ways code interacts with `scala.tools.nsc.typechecker.Analyzer`, as changed by [scala/scala#11306](https://github.com/scala/scala/pull/11306), compared with the alternative from [this comment](https://github.com/scala/scala/pull/11306#issuecomment-6233978016) (make `Analyzer` itself an abstract class).

No build tool: each directory under `clients/` is a handful of sources plus `common/Harness.scala`, which runs one in-memory compilation with the client's `Global`. `run.sh` compiles every client with every compiler, then runs each client's classes against every compiler's jars, and builds the single-page transcript `docs/index.html` (an index of cases, with code, source matrix and binary matrix for the selected one): <https://retronym.github.io/scala-analyzer-compat-demo/>.

```bash
./run.sh
```

Compilers (publish them locally first; see the commands in `run.sh`'s header):

| label | what | version |
|---|---|---|
| `18` | released 2.13.18 | `2.13.18` |
| `pr` | PR head: `trait Analyzer` self-typed `AbstractAnalyzer`, `Erasure` split out | `2.13.99` |
| `alt` | PR tree with `AbstractAnalyzer` folded into `abstract class Analyzer` (the `Erasure` split is needed too) | `2.13.98` |

Clients

| client | shape |
|---|---|
| `instantiate-trait` | `new { val global = g } with Analyzer [with MacroAnnotationNamers]` (Ammonite 3.0.9) |
| `instantiate-abstract` | the same with `AbstractAnalyzer` |
| `shim` | one source; a one-file `AnalyzerBase` per version (`old/`, `new/`) |
| `no-override` | don't instantiate it; steer macros through `findMacroClassLoader` (retronym/Ammonite#1) |
| `reference` | `global.analyzer.addAnalyzerPlugin(...)` only (sbt bridge style) |
| `interactive` | `new { ... } with InteractiveAnalyzer` (Ammonite's Pressy) |
| `macro-global` | a macro downcasting `c.universe` to `Global` and calling `global.analyzer` (not public API, but `_2.13` libraries do it, so binary compatibility matters) |
| `macro-symboltable` | a macro downcasting to `scala.reflect.internal.SymbolTable` |
| `shapeless` | published `shapeless_2.13:2.3.13` ([`generic.scala`](https://github.com/milessabin/shapeless/blob/5f5a3d4398174f0365bbc9e40a659c64066d15e7/core/shared/src/main/scala/shapeless/generic.scala#L395)) deriving `Generic`/`LabelledGeneric`: the in-the-wild `asInstanceOf[Global]` macro, plus `global.analyzer.Typer`/`Context` casts |
| `macro-scalameta` | replica of scalameta's `ConversionMacros.typeMismatchMessage`: `g.analyzer.foundReqMsg(...)`, a method called directly through the `Analyzer` type |
| `macro-monix` | replica of monix's `InlineMacros.stripUnApplyNodes`: `global.nme` from the downcast `Global` |
| `macro-cvogt` | replica of cvogt/scala-extensions' `EnumerateSingletons`: `global.TypeTreeWithDeferredRefCheck` |
| `macro-upickle-pprint` | replica of jrudolph/upickle-pprint's `Derive`: `global.analyzer.Typer.context.isAccessible` and `SymbolTable.treeBuild` |
| `macro-phantom` | replica of phantom's `HListHelpers.prefix`: `Type.prefix` through the downcast `Global` |
| `play-json-extensions` | published `play-json-extensions_2.13:0.42.0`; broken on 2.13.18 already (`TypeTreeWithDeferredRefCheck` constructor changed), so a baseline control, not PR fallout |
