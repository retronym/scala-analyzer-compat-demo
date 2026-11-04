import sys, statistics, collections
raw, java, forks, warm, meas = sys.argv[1:6]
rows = collections.defaultdict(lambda: collections.defaultdict(list))
for line in open(raw):
    l, k, f, ms = line.split("\t"); rows[(int(k), l)][int(f)].append(float(ms))
labels = {"base": "2.13.x (merge-base)", "pr": "scala/scala#11306", "third": "trait Analyzer extends AbstractAnalyzer", "alt": "abstract class Analyzer"}
print("# Synthetic benchmark\n")
print(f"Compile a generated corpus (120 case classes) in a warm JVM, {forks} forks, {warm} warmup + {meas} measured iterations, each iteration with one of K `Global`s with analyzers of distinct classes. `{open(java).read().strip()}`. ms/op, mean of fork means ± stdev of fork means.\n")
print("| variant | K=1 | K=4 | K=4 vs base |\n|---|---|---|---|")
def stat(k, l):
    fm = [statistics.mean(v) for v in rows[(k, l)].values()]
    return statistics.mean(fm), (statistics.stdev(fm) if len(fm) > 1 else 0.0)
base4 = stat(4, "base")[0] if rows.get((4, "base")) else None
for l in ["base", "pr", "third", "alt"]:
    if not rows.get((1, l)): continue
    m1, s1 = stat(1, l); m4, s4 = stat(4, l)
    d = "" if l == "base" or not base4 else f"{(m4 / base4 - 1) * 100:+.1f}%"
    print(f"| {labels[l]} | {m1:.1f} ± {s1:.1f} | {m4:.1f} ± {s4:.1f} | {d} |")
