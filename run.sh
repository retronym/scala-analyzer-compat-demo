#!/usr/bin/env bash
# Regenerates docs/index.html: every client compiled against every compiler (source compat), and the classes compiled
# against one compiler run against each of the others (binary compat). Needs `coursier` and the compilers below.
#
#   18  released 2.13.18
#   pr  scala/scala#11306 (trait Analyzer self-typed AbstractAnalyzer), published locally as 2.13.99
#   alt the comment-variant (abstract class Analyzer), published locally as 2.13.98
set -u
cd "$(dirname "$0")"
declare -A VER=( [18]=2.13.18 [pr]=2.13.99 [alt]=2.13.98 )
ORDER=(18 pr alt)
OUT=TRANSCRIPT.md
WORK=${TMPDIR:-/tmp}/analyzer-compat-demo; rm -rf "$WORK"; mkdir -p "$WORK"

cp_of() { # jars for a compiler label
  local v=${VER[$1]}; local f=$WORK/cp-$1.txt
  [ -s "$f" ] || coursier fetch -r ivy2Local -r central --classpath \
    org.scala-lang:scala-compiler:$v org.scala-lang:scala-reflect:$v org.scala-lang:scala-library:$v \
    --force-version org.scala-lang:scala-library:$v --force-version org.scala-lang:scala-reflect:$v \
    --force-version org.scala-lang:scala-compiler:$v > "$f" 2>/dev/null
  cat "$f"
}
srcs() { # sources of a client for compiler label $2
  local c=$1 l=$2
  if [ -d clients/$c/shared ]; then
    local variant=old; [ "$l" = pr ] && variant=new
    echo clients/$c/shared/*.scala clients/$c/$variant/*.scala common/*.scala
  else echo clients/$c/*.scala common/*.scala; fi
}
first() { grep -m1 -E '^(ok|FAIL|Exception|java\.|Error)' | sed -e 's/^Exception in thread "main" //' -e 's/ (Client.*//' | cut -c1-170; }

CLIENTS=$(ls clients)
RES=$WORK/res; mkdir -p $RES
for c in $CLIENTS; do
  for l in "${ORDER[@]}"; do
    d=$WORK/classes-$c-$l; mkdir -p $d
    if msg=$(java -cp "$(cp_of $l)" scala.tools.nsc.Main -usejavacp:false -nowarn -classpath "$(cp_of $l)" -d $d $(srcs $c $l) 2>&1); then
      echo ok > $RES/src-$c-$l.status
    else
      echo fail > $RES/src-$c-$l.status; echo "$msg" > $RES/src-$c-$l.msg; rm -rf $d
    fi
  done
  for cl in "${ORDER[@]}"; do
    [ -d $WORK/classes-$c-$cl ] || continue
    src=; [ -f clients/$c/source.txt ] && src=$(cat clients/$c/source.txt)
    deps=; [ -f clients/$c/deps.txt ] && deps=:$(coursier fetch --classpath -E org.scala-lang:scala-library -E org.scala-lang:scala-reflect -E org.scala-lang:scala-compiler $(cat clients/$c/deps.txt))
    for rl in "${ORDER[@]}"; do
      out=$(java ${src:+"-Ddemo.source=$src"} -cp "$WORK/classes-$c-$cl:$(cp_of $rl)$deps" Main 2>&1)
      echo "$out" > $RES/bin-$c-$cl-$rl.msg
      case "$out" in ok*) echo ok;; *) echo fail;; esac > $RES/bin-$c-$cl-$rl.status
    done
  done
done
for l in "${ORDER[@]}"; do echo "${VER[$l]}" > $RES/ver-$l; done
python3 build_html.py "$RES" docs/index.html
