package parse;
import java.util.List;
import errorMsg.*;
import syntaxtree.*;

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

	//: <class decl> ::= `class # ID `{ <decl in class>* `} =>
	public ClassDecl createClassDecl(int pos, String name, List<Decl> vec) {
		return new ClassDecl(pos, name, "Object", new DeclList(vec));
	}

	//: <decl in class> ::= <method decl> => pass

	//: <method decl> ::= `public `void # ID `( `) `{ <stmt>* `} =>
	public Decl createMethodDeclVoid(int pos, String name, List<Statement> stmts) {
		return new MethodDeclVoid(pos, name, new VarDeclList(new VarDeclList()),
				new StatementList(stmts));
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

	//: <stmt> ::= <callExp> `; =>
	public Statement newCallStatement(Exp aobj, int pos,  String aMethName, ExpList aparams)
	{
		if(aparams == null) aparams = new ExpList();
		Call call = new Call(pos, aobj, aMethName, aparams);
		return new CallStatement(pos, call);
	}
	//: <stmt> ::= <callExp2> `; =>
	public Statement newCallThisStatement(int pos, String aMethName, ExpList aparams)
	{
		if(aparams == null) aparams = new ExpList();
		Exp newThis = new This(pos);
		Call call = new Call(pos, newThis, aMethName, aparams);
		return new CallStatement(pos, call);
	}
	//: <stmt> ::= <callExp3> `; =>
	public Statement newCallSuperStatement(int pos, String aMethName, ExpList aparams)
	{
		if(aparams == null) aparams = new ExpList();
		Exp newSuper = new Super(pos);
		Call call = new Call(pos, newSuper, aMethName, aparams);
		return new CallStatement(pos, call);
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
	public Exp newInstVarAccess(Exp e1, int pos, String id) { return new InstVarAccess(pos, e1, id); }
	//: <exp1> ::= # INTLIT =>
	public Exp newIntegerLiteral(int pos, int n) {
		return new IntegerLiteral(pos, n);
	}
	//: <exp1> ::= # STRINGLIT =>
	public Exp newStringLiteral(int pos, String s) { return new StringLiteral(pos, s); }
	//: <exp1> ::= `new # ID `( `) =>
	public Exp newNewObject(int pos, String id) { return new NewObject(pos, new IdentifierType(pos, id)); }

	//: <exp1> ::= <callExp> =>
	public Exp newCall(Exp aobj, int pos, String aMethName, ExpList aparams)
	{
		if(aparams == null) aparams = new ExpList();
		return new Call(pos, aobj, aMethName, aparams);
	}
	//: <exp1> ::= <callExp2> =>
	public Exp newCallThis(int pos, String aMethName, ExpList aparams)
	{

		if(aparams == null) aparams = new ExpList();
		Exp newThis = new This(pos);
		return new Call(pos, newThis, aMethName, aparams);
	}
	//: <exp1> ::= <callExp3> =>
	public Exp newCallSuper(int pos, String aMethName, ExpList aparams)
	{
		if(aparams == null) aparams = new ExpList();
		Exp newSuper = new Super(pos);
		return new Call(pos, newSuper, aMethName, aparams);
	}

	//	STUCK ON THIS ONE
	////: <exp1> ::=  `( <exp> `) => pass

	//: <callExp> ::= <exp1> `. # ID `( <expList>? `) => pass
	//: <callExp2> ::= # ID `( <expList>? `) => pass
	//: <callExp3> ::= `super `. # ID `( <expList>? `) => pass


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
