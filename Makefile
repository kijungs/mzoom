all: compile demo
compile:
	-chmod u+x ./*.sh
	./compile.sh
demo:
	-chmod u+x ./*.sh
	rm -rf output
	mkdir output
	@echo [DEMO] running M-zoom...
	./run.sh example_data.txt output 7 geo 3
	@echo [DEMO] found blocks were saved in output
