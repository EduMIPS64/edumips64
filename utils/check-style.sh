#!/bin/bash
ARTISTIC_STYLE_OPTIONS="--indent=spaces=2 --brackets=attach --break-blocks \
--pad-oper --pad-header --unpad-paren --add-brackets --convert-tabs \
--mode=java"
ASTYLE=$(which astyle)

if [ "$ASTYLE" == "" ]; then
  echo "The astyle binary cannot be found. Exiting."
  exit 1
fi

case $# in
  1)
    DIR=$1
    VERIFY=false ;;
  2)
    DIR=$1
    VERIFY=true ;;
  *)
    echo "Wrong number of arguments. The first argument is mandatory, and is \
          the root of the edumips64 source code; if a second argument is \
          passed, the script will exit with a non-zero status if any style \
          error is found."
    exit 2 ;;
esac

FILES=$(find $1 -name "*.java")
for f in $FILES; do
  $ASTYLE $ARTISTIC_STYLE_OPTIONS < $f > $f.fixed
  if ! diff $f $f.fixed; then
    if [ "$VERIFY" == "true" ]; then
      rm -f $f.fixed $f.patch
      exit 1;
    fi
    echo "File $f has style problems."
    read -p "Apply the patch? (y/n) " apply
    if [[ $apply = [Yy] ]]; then
      mv $f.fixed $f
      echo "Patch applied."
    else
      echo "Patch not applied."
    fi
  fi
  rm -f $f.fixed $f.patch
done
