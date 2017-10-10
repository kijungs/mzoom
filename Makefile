all: compile demo
compile:
	-chmod u+x ./*.sh
	./compile.sh
demo:
	-chmod u+x ./*.sh
	rm -rf output_mzoom
	mkdir output_mzoom
	@echo [DEMO] running M-zoom...
	./run_mzoom.sh example_data.txt output_mzoom 7 geo 3
	@echo [DEMO] found blocks were saved in output
	rm -rf output_mbiz
	mkdir output_mbiz
	@echo [DEMO] running M-biz...
	./run_mbiz.sh example_data.txt output_mbiz 7 geo 3
	@echo [DEMO] found blocks were saved in output
