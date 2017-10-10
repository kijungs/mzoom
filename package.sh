rm MZoom-2.0.tar.gz
rm -rf MZoom-2.0
mkdir MZoom-2.0
cp -R ./{run_mzoom.sh,run_mbiz.sh,compile.sh,package.sh,src,./output,Makefile,README.txt,*.jar,example_data.txt,user_guide.pdf} ./MZoom-2.0
tar cvzf MZoom-2.0.tar.gz --exclude='._*' ./MZoom-2.0
rm -rf MZoom-2.0
echo done.