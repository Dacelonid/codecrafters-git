#!/bin/bash
set -e

# Usage: ./setup_git_test_repo.sh /path/to/destination

DEST_DIR="$1"
if [[ -z "$DEST_DIR" ]]; then
  echo "Usage: $0 /path/to/destination"
  exit 1
fi

REPO_DIR=$(mktemp -d)

echo "Creating Git repo in $REPO_DIR"

cd "$REPO_DIR"

git init

mkdir -p dir1/dir11 dir1/dir12 dir2

echo "apple banana cherry flavour" > file1.txt
echo "apple banana cherry" > dir1/file1.txt
echo "dog elephant frog" > dir1/dir11/file2.txt
echo "hat igloo jackal" > dir1/dir11/file3.txt
echo "kite lion monkey" > dir1/dir12/file4.txt
echo "nose octopus penguin" > dir2/file5.txt
echo "queen rabbit snake" > dir2/file6.txt

git add .
git commit -m "Add test files for directory structure"

LATEST_COMMIT=$(git rev-parse HEAD)
ROOT_TREE=$(git rev-parse HEAD^{tree})

echo "Latest commit: $LATEST_COMMIT"
echo "Root tree SHA1: $ROOT_TREE"

echo
echo "File -> Blob SHA1:"
git ls-files -s | while read -r mode sha rest; do
  # ls-files -s output: mode SHA stage TAB filename
  filename=$(echo "$rest" | cut -f2- -d$'\t')
  echo "$filename -> $sha"
done

mkdir -p "$DEST_DIR"
echo "Copying .git/objects to $DEST_DIR"
cp -r .git/objects "$DEST_DIR"

echo "Done."
echo "Repo directory was: $REPO_DIR"

