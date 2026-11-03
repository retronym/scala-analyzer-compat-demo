#!/usr/bin/env python3
"""Builds the single-page transcript (docs/index.html) from the per-cell results run.sh leaves in a directory."""
import json, os, sys, html, re

res, out = sys.argv[1], sys.argv[2]
ORDER = ["18", "pr", "alt", "third"]
read = lambda p: open(p).read().strip() if os.path.exists(p) else None

def short(msg):
    for line in (msg or "").splitlines():
        if re.match(r"^(ok|FAIL|Exception|java\.|Error)", line):
            line = re.sub(r'^Exception in thread "main" ', "", line)
            return line[:200]
    return (msg or "no output")[:200]

cases = []
for c in sorted(os.listdir("clients")):
    files = []
    for root, _, names in os.walk(f"clients/{c}"):
        for n in sorted(names):
            if n != "DESCRIPTION.txt":
                p = os.path.join(root, n)
                files.append({"path": p, "text": open(p).read()})
    files.sort(key=lambda f: f["path"])
    case = {"name": c, "description": read(f"clients/{c}/DESCRIPTION.txt"), "files": files, "source": {}, "binary": {}}
    for l in ORDER:
        st = read(f"{res}/src-{c}-{l}.status")
        case["source"][l] = {"ok": st == "ok", "msg": read(f"{res}/src-{c}-{l}.msg")}
    for cl in ORDER:
        if case["source"][cl]["ok"]:
            case["binary"][cl] = {}
            for rl in ORDER:
                m = read(f"{res}/bin-{c}-{cl}-{rl}.msg")
                case["binary"][cl][rl] = {"ok": read(f"{res}/bin-{c}-{cl}-{rl}.status") == "ok", "short": short(m), "msg": m}
    cases.append(case)

data = {"versions": {l: read(f"{res}/ver-{l}") for l in ORDER},
        "labels": {"18": "released", "pr": "scala/scala#11306", "alt": "abstract class Analyzer", "third": "trait Analyzer extends AbstractAnalyzer"},
        "cases": cases}
tmpl = open("template.html").read()
os.makedirs(os.path.dirname(out), exist_ok=True)
open(out, "w").write(tmpl.replace("/*DATA*/", json.dumps(data).replace("</", "<\\/")))
print("wrote", out)
