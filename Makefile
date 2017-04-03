LIB_ANTLR := lib/antlr.jar
ANTLR_SCRIPT := Micro.g4

all: group compiler

group:
	@echo "Kareem El Azhary, Sthitapragyan Parida"
compiler:
	@rm -rf build
	@rm -rf classes
	@mkdir build
	@mkdir classes
	@java -cp $(LIB_ANTLR) org.antlr.v4.Tool -o build $(ANTLR_SCRIPT)
	@javac -cp $(LIB_ANTLR) -Xlint:unchecked -d classes src/*.java build/*.java
clean:
	@rm -rf classes build
	@rm -rf testcases/myoutputs
	@rm -rf testcases/tiny

.PHONY: all group compiler clean

test:
	@rm -rf testcases/myoutputs
	@rm -rf testcases/tiny
	@g++ -o testcases/tiny testcases/tiny4regs.C
	@mkdir testcases/myoutputs

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/fma.micro > testcases/myoutputs/fma.myout
	@testcases/tiny testcases/myoutputs/fma.myout < testcases/input/fma.input > testcases/myoutputs/fma.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/factorial2.micro > testcases/myoutputs/factorial2.myout
	@testcases/tiny testcases/myoutputs/factorial2.myout < testcases/input/factorial2.input > testcases/myoutputs/factorial2.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/fibonacci2.micro > testcases/myoutputs/fibonacci2.myout
	@testcases/tiny testcases/myoutputs/fibonacci2.myout < testcases/input/fibonacci2.input > testcases/myoutputs/fibonacci2.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/owntestcase.micro > testcases/myoutputs/owntestcase.myout
	@testcases/tiny testcases/myoutputs/owntestcase.myout < testcases/input/owntestcase.input > testcases/myoutputs/owntestcase.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/step4_testcase.micro > testcases/myoutputs/step4_testcase.myout
	@testcases/tiny testcases/myoutputs/step4_testcase.myout < testcases/input/step4_testcase.input > testcases/myoutputs/step4_testcase.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/step4_testcase2.micro > testcases/myoutputs/step4_testcase2.myout
	@testcases/tiny testcases/myoutputs/step4_testcase2.myout < testcases/input/step4_testcase2.input > testcases/myoutputs/step4_testcase2.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/step4_testcase3.micro > testcases/myoutputs/step4_testcase3.myout
	@testcases/tiny testcases/myoutputs/step4_testcase3.myout < testcases/input/step4_testcase3.input > testcases/myoutputs/step4_testcase3.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/test_adv.micro > testcases/myoutputs/test_adv.myout
	@testcases/tiny testcases/myoutputs/test_adv.myout < testcases/input/test_adv.input > testcases/myoutputs/test_adv.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/test_if.micro > testcases/myoutputs/test_if.myout
	@testcases/tiny testcases/myoutputs/test_if.myout < testcases/input/test_if.input > testcases/myoutputs/test_if.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/test_expr.micro > testcases/myoutputs/test_expr.myout
	@testcases/tiny testcases/myoutputs/test_expr.myout < testcases/input/test_expr.input > testcases/myoutputs/test_expr.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/test_dowhile.micro > testcases/myoutputs/test_dowhile.myout
	@testcases/tiny testcases/myoutputs/test_dowhile.myout < testcases/input/test_dowhile.input > testcases/myoutputs/test_dowhile.mytinyout

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/step8functions.micro > testcases/myoutputs/step8functions.myout
	@testcases/tiny testcases/myoutputs/step8functions.myout < testcases/input/step8functions.input > testcases/myoutputs/step8functions.mytinyout

test_step8:
	@rm -rf testcases/myoutputs
	@rm -rf testcases/tiny
	@g++ -o testcases/tiny testcases/tiny4regs.C
	@mkdir testcases/myoutputs

	@java -cp lib/antlr.jar:classes/ Micro testcases/input/step8functions.micro > testcases/myoutputs/step8functions.myout
	@testcases/tiny testcases/myoutputs/step8functions.myout < testcases/input/step8functions.input > testcases/myoutputs/step8functions.mytinyout


clean_test: clean compiler test	
