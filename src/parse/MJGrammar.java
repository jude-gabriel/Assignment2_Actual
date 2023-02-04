/************************************************************************************
 * Compilers Assignment 2
 *
 * Jude Gabriel
 * February 4, 2023
 *
 * ENHANCEMENTS:
 * Extension 1:
 * 	Recognize do while loops
 *
 * Extension 2:
 * 	Recognize switch statements
 * ***********************************************************************************/


package parse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import errorMsg.*;
import syntaxtree.*;

import javax.swing.plaf.nimbus.State;

public class MJGrammar
		implements wrangLR.runtime.MessageObject,
			   wrangLR.runtime.FilePosObject {
	
	// tells whether to use the filtered grammar
	public static final boolean FILTER_GRAMMAR = true;
	
	// constructor
	// @param em error-message object
	public MJGrammar(ErrorMsg em) {
		errorMsg = em;
		topObject = null;
	}
	
	// error message object
	private ErrorMsg errorMsg;
	
	// object to be returned by the parser
	private Program topObject;

	// method for printing error message and marking that
    // compilation has failed
	// @param pos the file position for error message
	// @param msg the error message
	public void error(int pos, String msg) {
		errorMsg.error(pos, msg);
	}

	// method for printing warning message
	// @param pos file position for warning message
	// @param msg the warning message
	public void warning(int pos, String msg) {
		errorMsg.warning(pos, msg);
	}

	// method for converting file position to line/char position
	// @param pos the file position
	// @return the string that denotes the file position
	public String filePosString(int pos) {
		return errorMsg.lineAndChar(pos);
	}
	
	// method that registers a newline
	// @param pos the file position of the newline character
	public void registerNewline(int pos) {
		errorMsg.newline(pos-1);
	}

	// returns the object produced by the parse
	// @return the top-level object produced by the parser
	public Program parseResult() {
		return topObject;
	}
	
	//===============================================================
	// start symbol
	//===============================================================

	//: <start> ::= ws* <program> =>   
	public void topLevel(Program obj) {
		topObject = obj;
	}
	
	//================================================================
	// top-level constructs
	//================================================================

	//: <program> ::= # <class decl>+ =>
	public Program createProgram(int pos, List<ClassDecl> vec) {
		return new Program(pos, new ClassDeclList(vec));
	}



	/********* Class and Method Decl ************/

	//: <class decl> ::= `class # ID classExtends? `{ <decl in class>* `} =>
	public ClassDecl createClassDecl(int pos, String name, String extended, List<Decl> vec) {
		if(extended == null)
		{
			return new ClassDecl(pos, name, "Object", new DeclList(vec));
		}
		return new ClassDecl(pos, name, extended, new DeclList(vec));
	}

	//: classExtends ::= `extends ID => pass

	//: <decl in class> ::= <method decl> => pass
	//: <decl in class> ::= <inst var decl> => pass
	//: <inst var decl> ::= # <type> ID `; =>
	public Decl instanceVarDecl(int pos, Type t, String name)
	{
		return new InstVarDecl(pos, t, name);
	}

	//: <method decl> ::= `public <type> # ID `( formalList? `) `{ <stmt>* `return <exp> `; `} =>
	public Decl createMethodDeclNonVoid(Type t, int pos, String name, List<VarDecl> vars, List<Statement> stmts, Exp exp)
	{
		if(vars == null)
		{
			return new MethodDeclNonVoid(pos, t, name, new VarDeclList(new VarDeclList()), new StatementList(stmts), exp);
		}
		return new MethodDeclNonVoid(pos, t, name, new VarDeclList(vars), new StatementList(stmts), exp);
	}

	//: <method decl> ::= `public `void # ID `( formalList? `) `{ <stmt>* `} =>
	public Decl createMethodDeclVoid(int pos, String name, List<VarDecl> vars, List<Statement> stmts) {
		if( vars == null)
		{
			return new MethodDeclVoid(pos, name, new VarDeclList( new VarDeclList()), new StatementList(stmts));
		}
		return new MethodDeclVoid(pos, name, new VarDeclList(vars), new StatementList(stmts));
	}

	//: formalList ::= <type> # ID formalListCont* =>
	public List<VarDecl> formalDeclParams(Type t, int pos, String name, List<FormalDecl> list)
	{
		FormalDecl formalDecl = new FormalDecl(pos, t, name);
		if(list == null)
		{
			List<VarDecl> arrList = Arrays.asList(formalDecl);
			return new VarDeclList(arrList);
		}
		else
		{
			list.add(0, formalDecl);
			List<VarDecl> arrList = new ArrayList<>();
			for(int i = 0; i < list.size(); i++)
			{
				arrList.add(i, list.get(i));
			}
			return new VarDeclList(arrList);
		}
	}

	//: formalListCont ::= `, <type> # ID =>
	public FormalDecl extraFormalParams(Type t, int pos, String name)
	{
		return new FormalDecl(pos, t, name);
	}






	//// TYPES ////

	//: <type> ::= # `int =>
	public Type intType(int pos) {
		return new IntegerType(pos);
	}
	//: <type> ::= # `boolean =>
	public Type booleanType(int pos) {
		return new BooleanType(pos);
	}
	//: <type> ::= # ID =>
	public Type identifierType(int pos, String name) {
		return new IdentifierType(pos, name);
	}
	//: <type> ::= <type> # <empty bracket pair> =>
	public Type newArrayType(Type t, int pos, Object dummy) {
		return new ArrayType(pos, t);
	}

	//: <empty bracket pair> ::= `[ `] => null

	//================================================================
	// statement-level constructs
	//================================================================

	//: <stmt> ::= <assign> `; => pass


	//: <stmt> ::= # `if `( <exp> `) <stmt> !`else # =>
	public Statement newIfNoNoElse(int pos, Exp ex, Statement s1, int pos2)
	{
		return new If(pos, ex, s1, new Block(pos2, new StatementList()));
	}
	//: <stmt> ::= # `if `( <exp> `) <stmt> `else <stmt> =>
	public Statement newIfWithElse(int pos, Exp ex, Statement s1, Statement s2)
	{
		return new If(pos, ex, s1, s2);
	}

	//: <stmt> ::= # `while `( <exp> `) <stmt> =>
	public Statement newWhile(int pos, Exp exp, Statement statement) { return new While(pos, exp, statement); }

	//: <stmt> ::= # `for `( # forAssignment? `; #<exp>? `; # forIncrement? `) # <stmt> =>
	public Statement newFor(int pos, int assignmentPos, Statement s1, int pos1, Exp e, int pos2, Statement s2, int pos3, Statement s3)
	{
		if(s1 == null)
		{
			s1 = new Block(assignmentPos, new StatementList());
		}
		List<Statement> list = Arrays.asList(s1);

		if(e == null)
		{
			e = new True(pos1);
		}

		if(s3 == null)
		{
			s3 = new Block(pos3, new StatementList());
		}

		if(s2 == null)
		{
			s2 = new Block(pos2, new StatementList());
		}

		List<Statement> whileBody = Arrays.asList(s3, s2);
		Block whileBlock = new Block(pos, new StatementList(whileBody));
		List<Statement> l2 = new ArrayList<>(list);
		l2.add(new While(pos, e, whileBlock));
		return new Block(pos, new StatementList(l2));
	}
	//: forAssignment ::= callStmt => pass
	//: forAssignment ::= <assign> => pass
	//: forAssignment ::= <local var decl> => pass
	//: forIncrement ::= callStmt => pass
	//: forIncrement ::= <assign> => pass
	//: callStmt ::= # <callExp> => Statement callStatementFor(int, Exp)
	//: callStmt ::= # <callExp2> => Statement callStatementFor(int, Exp)
	//: callStmt ::= # <callExp3> => Statement callStatementFor(int, Exp)
	public Statement callStatementFor(int pos, Exp call)
	{
		return new CallStatement(pos, (Call)call);
	}

	//: <stmt> ::= # `switch `( <exp> `) `{ switchType* `} =>
	public Statement newSwitch(int switchPos, Exp exp, List<Statement> s)
	{
		return new Switch(switchPos, exp, new StatementList(s));
	}
	//: switchType ::= <stmt> => pass
	//: switchType ::= # `case <exp> `: =>
	public Statement newCase(int pos, Exp exp)
	{
		return new Case(pos, exp);
	}
	//: switchType ::= # `default `: =>
	public Statement newDefault(int pos)
	{
		return new Default(pos);
	}

	//: <stmt> ::= # `do # `{ <stmt>* `} #`while `( # <exp> `) `; =>
	public Statement newDoWhile(int doPos, int stmtPos, List<Statement> stmt, int whilePos, int expPos, Exp exp)
	{
		Block stmtBlock = new Block(stmtPos, new StatementList(stmt));

		Not not = new Not(expPos, exp);
		Break newBreak = new Break(expPos);
		If newIf = new If(whilePos, not, newBreak, new Block(whilePos, new StatementList()));

		List <Statement> stmList2 = Arrays.asList(stmtBlock, newIf);

		True newTrue = new True(doPos);
		return new While(doPos, newTrue, new Block(doPos, new StatementList(stmList2)));
	}




	//: <stmt> ::= # `; =>
	public Statement newSemicolon(int pos) { return new Block(pos, new StatementList()); }

	//: <stmt> ::= # <callExp> `; =>
	public Statement newCallStatement(int pos,  Exp call )
	{
		return new CallStatement(pos, (Call) call);
	}
	//: <stmt> ::= # <callExp2> `; =>
	public Statement newCallThisStatement(int pos, Exp call)
	{
		return new CallStatement(pos, (Call) call);
	}
	//: <stmt> ::= # <callExp3> `; =>
	public Statement newCallSuperStatement(int pos, Exp call)
	{
		return new CallStatement(pos, (Call) call);
	}
	
	//: <stmt> ::= # `{ <stmt>* `} =>
	public Statement newBlock(int pos, List<Statement> sl) {
		return new Block(pos, new StatementList(sl));
	}

	//: <stmt> ::= # `break `; =>
	public Statement newBreak(int pos) {
		return new Break(pos);
	}

	//: <stmt> ::= <local var decl> `; => pass

	//// ASSIGNMENTS ////
	//: <assign> ::= ID # `++ =>
	public Statement incrementAfter(String id, int pos)
	{
		return new Assign(pos - id.length(), new IdentifierExp(pos - id.length(), id),
				new Plus(pos, new IdentifierExp(pos - id.length(), id), new IntegerLiteral(pos, 1)));
	}
	//: <assign> ::= ID # `-- =>
	public Statement decrementAfter(String id, int pos)
	{
		return new Assign(pos - id.length(), new IdentifierExp(pos - id.length(), id),
				new Minus(pos, new IdentifierExp(pos - id.length(), id), new IntegerLiteral(pos, 1)));
	}
	//: <assign> ::= # `++ ID  =>
	public Statement incrementBefore(int pos, String id)
	{
		return new Assign(pos, new IdentifierExp(pos, id),
				new Plus(pos + 2, new IdentifierExp(pos, id), new IntegerLiteral(pos+2, 1)));
	}
	//: <assign> ::= # `-- ID  =>
	public Statement decrementBefore(int pos, String id)
	{
		return new Assign(pos, new IdentifierExp(pos, id),
				new Minus(pos + 2, new IdentifierExp(pos, id), new IntegerLiteral(pos+2, 1)));
	}


	//: <assign> ::= <exp> # `= <exp> =>
	public Statement assign(Exp lhs, int pos, Exp rhs) {
		return new Assign(pos, lhs, rhs);
	}

	//: <local var decl> ::= <type> # ID `= <exp> =>
	public Statement localVarDecl(Type t, int pos, String name, Exp init) {
		return new LocalDeclStatement(pos, new LocalVarDecl(pos, t, name, init));
	}

	//================================================================
	// expressions
	//================================================================

	//: <exp> ::= <exp8> => pass
	
	// these precedence levels have not been filled in at all, so there
	// are only pass-through productions
	//: <exp8> ::= <exp7> => pass
	//: <exp7> ::= <exp6> => pass
	//: <exp6> ::= <exp5> => pass


	//// TYPE 8 EXP ////
	//: <exp8> ::= <exp8> # `|| <exp7> =>
	public Exp newOr(Exp ae1, int pos, Exp ae2) { return new Or(pos, ae1, ae2); }

	//// TYPE 7 EXP ////
	//: <exp7> ::= <exp7> # `&& <exp6> =>
	public Exp newAnd(Exp ae1, int pos, Exp ae2) { return new And(pos, ae1, ae2); }

	//// TYPE 6 EXP ////
	//: <exp6> ::= <exp6> # `== <exp5> =>
	public Exp newEquals(Exp ae1, int pos, Exp ae2) { return new Equals(pos, ae1, ae2); }

	//: <exp6> ::= <exp6> # `!= <exp5> =>
	public Exp newNotEquals(Exp ae1, int pos, Exp ae2)
	{
		return new Not(pos, new Equals(pos, ae1, ae2));
	}

	//// TYPE 5 EXP ////
	//: <exp5> ::= <exp5> # `instanceof <type> =>
	public Exp newInstanceOf(Exp ae1, int pos, Type at1) { return new InstanceOf(pos, ae1, at1); }


	//: <exp5> ::= <exp5> # `> <exp4> =>
	public Exp newGreaterThan(Exp ae1, int pos, Exp ae2) { return new GreaterThan(pos, ae1, ae2); }

	//: <exp5> ::= <exp5> # `< <exp4> =>
	public Exp newLessThan(Exp ae1, int pos, Exp ae2) { return new LessThan(pos, ae1, ae2); }

	//: <exp5> ::= <exp5> # `>= <exp4> =>
	public Exp newGreaterThanOrEqual(Exp ae1, int pos, Exp ae2)
	{
		return new Not(pos, new LessThan(pos, ae1, ae2));
	}

	//: <exp5> ::= <exp5> # `<= <exp4> =>
	public Exp newLessThanOrEqual(Exp ae1, int pos, Exp ae2)
	{
		return new Not(pos, new GreaterThan(pos, ae1, ae2));
	}


	//: <exp5> ::= <exp4> => pass

	
	//: <exp4> ::= <exp4> # `+ <exp3> =>
	public Exp newPlus(Exp e1, int pos, Exp e2) {
		return new Plus(pos, e1, e2);
	}
	//: <exp4> ::= <exp3> => pass

	//: <exp4> ::= <exp4> # `- <exp3> =>
	public Exp newMinus(Exp e1, int pos, Exp e2) { return new Minus(pos, e1, e2); }
	//: <exp4> ::= <exp3> => pass

	//: <exp3> ::= <exp3> # `* <exp2> =>
	public Exp newTimes(Exp e1, int pos, Exp e2) {
		return new Times(pos, e1, e2);
	}

	//: <exp3> ::= <exp3> # `% <exp2> =>
	public Exp newRemainder(Exp e1, int pos, Exp e2) {
		return new Remainder(pos, e1, e2);
	}

	//: <exp3> ::= <exp3> # `/ <exp2> =>
	public Exp newDivide(Exp e1, int pos, Exp e2) {
		return new Divide(pos, e1, e2);
	}
	//: <exp3> ::= <exp2> => pass

	//: <exp2> ::= <cast exp> => pass
	//: <exp2> ::= <unary exp> => pass

	/****** THIS MAY BE WRONG **********/
	//: <cast exp> ::= # `( <type> `) <cast exp> =>
	public Exp newCast(int pos, Type t, Exp e) {
		return new Cast(pos, t, e);
	}
	//: <cast exp> ::= # `( <type> `) <exp1> => Exp newCast(int, Type, Exp)

	//: <unary exp> ::= # `- <unary exp> =>
	public Exp newUnaryMinus(int pos, Exp e) {
		return new Minus(pos, new IntegerLiteral(pos, 0), e);
	}
	//: <unary exp> ::= <exp1> => pass

	//: <unary exp> ::= # `+ <unary exp> =>
	public Exp newUnaryPlus(int pos, Exp e) { return new Plus(pos, new IntegerLiteral(pos, 0), e); }

	//: <unary exp> ::= # `! <unary exp> =>
	public Exp newUnaryNot(int pos, Exp e) { return new Not(pos, e); }



	///////// TYPE 1 EXPR ////////
	//: <exp1> ::= # `true  =>
	public Exp newTrue(int pos) { return new True(pos); }
	//: <exp1> ::= # `false  =>
	public Exp newFalse(int pos) { return new False(pos); }

	//: <exp1> ::= # `null  =>
	public Exp newNull(int pos) { return new Null(pos); }

	//: <exp1> ::= # `this  =>
	public Exp newThis(int pos) { return new This(pos); }

	//: <exp1> ::= # ID  =>
	public Exp newIdentfierExp(int pos, String name) {
		return new IdentifierExp(pos, name);
	}
	//: <exp1> ::= <exp1> !<empty bracket pair> # `[ <exp> `] =>
	public Exp newArrayLookup(Exp e1, int pos, Exp e2) {
		return new ArrayLookup(pos, e1, e2);
	}
	//: <exp1> ::= <exp1>  # `. ID =>
	public Exp newInstVarAccess(Exp e1, int pos, String id)
	{
		if(Objects.equals(id, "length"))
		{
			return new ArrayLength(pos, e1);
		}
		return new InstVarAccess(pos, e1, id);
	}
	//: <exp1> ::= # INTLIT => Exp newIntegerLiteral(int, int)
	//: <exp1> ::= # CHARLIT => Exp newIntegerLiteral(int, int)
	public Exp newIntegerLiteral(int pos, int n) {
		return new IntegerLiteral(pos, n);
	}
	//: <exp1> ::= # STRINGLIT =>
	public Exp newStringLiteral(int pos, String s) { return new StringLiteral(pos, s); }
	//: <exp1> ::= `new # ID `( `) =>
	public Exp newNewObject(int pos, String id) { return new NewObject(pos, new IdentifierType(pos, id)); }

	//: <exp1> ::= # `new <type> `[ <exp> `] # <empty bracket pair>* =>
	public Exp newArray(int pos, Type t, Exp exp, int pos1, List<Object> list)
	{
		ArrayType arrayType = new ArrayType(pos, t);
		for(int i = 0; i < list.size(); i++)
		{
			arrayType = new ArrayType(pos1 + 2*i, arrayType);
		}
		return new NewArray(pos, arrayType, exp);
	}


	//: <exp1> ::= <callExp> =>pass
	//: <exp1> ::= <callExp2> => pass
	//: <exp1> ::= <callExp3> => pass
	//: <exp1> ::=  !<cast exp> `( <exp> `)  => pass

	//: <callExp> ::= <exp1> `. # ID `( <expList>? `) =>
	public Exp newCall(Exp aobj, int pos, String aMethName, ExpList aparams)
	{
		if(aparams == null) aparams = new ExpList();
		return new Call(pos, aobj, aMethName, aparams);
	}
	//: <callExp2> ::= # ID `( <expList>? `) =>
	public Exp newCallThis(int pos, String aMethName, ExpList aparams)
	{

		if(aparams == null) aparams = new ExpList();
		Exp newThis = new This(pos);
		return new Call(pos, newThis, aMethName, aparams);
	}
	//: <callExp3> ::= `super `. # ID `( <expList>? `) =>
	public Exp newCallSuper(int pos, String aMethName, ExpList aparams)
	{
		if(aparams == null) aparams = new ExpList();
		Exp newSuper = new Super(pos);
		return new Call(pos, newSuper, aMethName, aparams);
	}


	// Expression List
	//: <expList> ::= <exp> expListExtended* =>
	public ExpList newExpList(Exp exp, List<Exp> expList)
	{
		expList.add(0, exp);
		return new ExpList(expList);
	}
	//: expListExtended ::= `, <exp> => pass























	//================================================================
	// Lexical grammar for filtered language begins here: DO NOT
	// MODIFY ANYTHING BELOW THIS, UNLESS YOU REPLACE IT WITH YOUR
	// ENTIRE LEXICAL GRAMMAR, and set the constant FILTER_GRAMMAR
	// (defined near the top of this file) to false.
	//================================================================

	//: letter ::= {"a".."z" "A".."Z"} => pass
	//: letter128 ::= {225..250 193..218} =>
	public char sub128(char orig) {
		return (char)(orig-128);
	}
	//: digit ::= {"0".."9"} => pass
	//: digit128 ::= {176..185} => char sub128(char)
	//: any ::= {0..127} => pass
	//: any128 ::= {128..255} => char sub128(char)
	//: ws ::= " "
	//: ws ::= {10} registerNewline
	//: registerNewline ::= # => void registerNewline(int)
	//: `boolean ::= "#bo" ws*
	//: `class ::= "#cl" ws*
	//: `extends ::= "#ex" ws*
	//: `void ::= "#vo" ws*
	//: `int ::= "#it" ws*
	//: `while ::= "#wh" ws*
	//: `if ::= '#+' ws*
	//: `else ::= "#el" ws*
	//: `for ::= "#fo" ws*
	//: `break ::= "#br" ws*
	//: `this ::= "#th" ws*
	//: `false ::= '#fa' ws*
	//: `true ::= "#tr" ws*
	//: `super ::= "#su" ws*
	//: `null ::= "#nu" ws*
	//: `return ::= "#re" ws*
	//: `instanceof ::= "#in" ws*
	//: `new ::= "#ne" ws*
	//: `case ::= "#ce" ws*
	//: `default ::= "#de" ws*
	//: `do ::= "#-" ws*
	//: `public ::= "#pu" ws*
	//: `switch ::= "#sw" ws*
	
	//: `! ::=  "!" ws* => void
	//: `!= ::=  "@!" ws* => void
	//: `% ::= "%" ws* => void
	//: `&& ::= "@&" ws* => void
	//: `* ::= "*" ws* => void
	//: `( ::= "(" ws* => void
	//: `) ::= ")" ws* => void
	//: `{ ::= "{" ws* => void
	//: `} ::= "}" ws* => void
	//: `- ::= "-" ws* => void
	//: `+ ::= "+" ws* => void
	//: `= ::= "=" ws* => void
	//: `== ::= "@=" ws* => void
	//: `[ ::= "[" ws* => void
	//: `] ::= "]" ws* => void
	//: `|| ::= "@|" ws* => void
	//: `< ::= "<" ws* => void
	//: `<= ::= "@<" ws* => void
	//: `, ::= "," ws* => void
	//: `> ::= ">"  !'=' ws* => void
	//: `>= ::= "@>" ws* => void
	//: `: ::= ":" ws* => void
	//: `. ::= "." ws* => void
	//: `; ::= ";" ws* => void
	//: `++ ::= "@+" ws* => void
	//: `-- ::= "@-" ws* => void
	//: `/ ::= "/" ws* => void

	
	//: ID ::= letter128 ws* => text
	//: ID ::= letter idChar* idChar128 ws* => text
	
	//: INTLIT ::= {"1".."9"} digit* digit128 ws* => 
	public int convertToInt(char c, List<Character> mid, char last) {
		return Integer.parseInt(""+c+mid+last);
	}
	//: INTLIT ::= digit128 ws* => 
	public int convertToInt(char c) {
		return Integer.parseInt(""+c);
	}
	//: INTLIT ::= "0" hexDigit* hexDigit128 ws* => 
	public int convert16ToInt(char c, List<Character> mid, char last) {
		return Integer.parseInt(""+c+mid+last, 16);
	}
	//: STRINGLIT ::= '@"' ws* =>
	public String emptyString(char x, char xx) {
		return "";
	}
	//: STRINGLIT ::= '"' any* any128 ws* =>
	public String string(char x, List<Character> mid, char last) {
		return ""+mid+last;
	}
	//: CHARLIT ::= "'" any ws* =>
	public int charVal(char x, char val) {
		return val;
	}
	
	//: idChar ::= letter => pass
	//: idChar ::= digit => pass
	//: idChar ::= "_" => pass
	//: idChar128 ::= letter128 => pass
	//: idChar128 ::= digit128 => pass
	//: idChar128 ::= {223} => 
	public char underscore(char x) {
		return '_';
	}
	//: hexDigit ::= {"0".."9" "A".."Z" "a".."z"} => pass
	//: hexDigit128 ::= {176..185 225..230 193..198} => char sub128(char)

}
