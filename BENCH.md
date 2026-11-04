# Synthetic benchmark

Compile a generated corpus (120 case classes) in a warm JVM, 3 forks, 30 warmup + 40 measured iterations, each iteration with one of K `Global`s with analyzers of distinct classes. `openjdk version "21.0.12.1" 2026-08-18 LTS`. ms/op, mean of fork means ± stdev of fork means.

| variant | K=1 | K=4 | K=4 vs base |
|---|---|---|---|
| 2.13.x (merge-base) | 354.0 ± 53.7 | 437.7 ± 28.7 |  |
| scala/scala#11306 | 347.2 ± 63.5 | 329.1 ± 8.9 | -24.8% |
| trait Analyzer extends AbstractAnalyzer | 336.8 ± 63.0 | 329.4 ± 9.3 | -24.8% |
| abstract class Analyzer | 338.6 ± 61.0 | 337.6 ± 0.6 | -22.9% |
