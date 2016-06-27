rm MZoom-1.0.tar.gz
rm -rf MZoom-1.0
mkdir MZoom-1.0
cp -R ./{run.sh,compile.sh,package.sh,src,./output,Makefile,README.txt,*.jar,example_data.txt,user_guide.pdf} ./MZoom-1.0
tar cvzf MZoom-1.0.tar.gz --exclude='._*' ./MZoom-1.0
rm -rf MZoom-1.0
echo done.