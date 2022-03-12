package ctgen;

import org.tzi.use.uml.ocl.expr.Expression;

public class DynamicExpression {
	Expression e;
	String s;
	
	public String getString() {
		return this.s;
	}
	
	public Expression getExpression() {
		return this.e;
	}
	
	public void setString(String s) {
		this.s = new String(s);
	}
	
	public void setExpression(Expression e) {
		this.e = e;
	}
	
	public DynamicExpression(String s, Expression e) {
		this.setString(s);
		this.setExpression(e);
	}
	
	public DynamicExpression(String s) {
		this(s, null);
	}
	
	public DynamicExpression(Expression e) {
		this(null, e);
	}
	
	public DynamicExpression() {
		this(null, null);
	}
	
	public String toString() {
		return this.s;
	}
}
