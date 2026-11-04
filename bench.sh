#!/usr/bin/env bash
# Synthetic perf comparison of the Analyzer variants. Usage: ./bench.sh [forks] [warmup] [measured]
# Needs the compilers published locally (see run.sh) and 2.13.96 = the PR's merge-base, as the baseline.
set -u
cd "$(dirname "$0")"
FORKS=${1:-3}; WARM=${2:-30}; MEAS=${3:-40}
declare -A VER=( [base]=2.13.96 [pr]=2.13.99 [third]=2.13.97 [alt]=2.13.98 )
ORDER=(base pr third alt)
WORK=${TMPDIR:-/tmp}/analyzer-compat-demo-bench; rm -rf "$WORK"; mkdir -p "$WORK"
cp_of() { local v=${VER[$1]}; coursier fetch -r ivy2Local -r central --classpath \
    org.scala-lang:scala-compiler:$v org.scala-lang:scala-reflect:$v org.scala-lang:scala-library:$v \
    --force-version org.scala-lang:scala-library:$v --force-version org.scala-lang:scala-reflect:$v --force-version org.scala-lang:scala-compiler:$v 2>/dev/null; }
for l in "${ORDER[@]}"; do
  variant=old; [ "$l" = pr ] && variant=new
  mkdir -p $WORK/classes-$l
  java -cp "$(cp_of $l)" scala.tools.nsc.Main -nowarn -usejavacp:false -classpath "$(cp_of $l)" -d $WORK/classes-$l bench/Bench.scala clients/shim/$variant/AnalyzerBase.scala || exit 1
done
java -version 2>&1 | head -1 > $WORK/java.txt
: > $WORK/raw.tsv
for fork in $(seq 1 $FORKS); do            # variants interleaved fork by fork
  for k in 1 4; do
    for l in "${ORDER[@]}"; do
      java -Xms4G -Xmx4G -Xss16M -cp "$WORK/classes-$l:$(cp_of $l)" Bench $k $WARM $MEAS 2>&1 | awk -v l=$l -v k=$k -v f=$fork '/^iter/{print l"\t"k"\t"f"\t"$3} /^FAIL/{print "FAIL "l" "$0 > "/dev/stderr"}' >> $WORK/raw.tsv
    done
  done
done
python3 bench_report.py $WORK/raw.tsv $WORK/java.txt "$FORKS" "$WARM" "$MEAS" > BENCH.md
cat BENCH.md
