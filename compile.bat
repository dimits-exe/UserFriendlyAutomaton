dir /A-D /B /S src > .files

javac -d bin --release 11 @.files

del .files

pause
