package com.craftinginterpreters.lox;

/**
 * 解释器-计算值
 */
class Interpreter implements Expr.Visitor<Object>{

    /**
     * 供外部调用接口，目的是为了调用核心的visit方法
     * @param expression
     */
    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator,left,right);
                return (double)left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left <= (double) right;
            case MINUS:
                checkNumberOperand(expr.operator,right);
                return (double)left - (double) right;
            case PLUS:
                if (left instanceof  Double && right instanceof Double) {
                    return (double)left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    //那如果是左边string右边数字呢，或者相反
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator,
                        "Operands must two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator,left,right);
                return (double)left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator,left,right);
                return (double)left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        //其他情况
        return null;
    }

    /**
     * 获取括号内表达式的内部节点
     * @param expr
     * @return
     */
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG:
                //取反
                return !isTruthy(right);
            case MINUS:
                return -(double)right;
        }
        //其他情况返回空
        return null;
    }

    /**
     * 检查数据是否满足条件，不满足抛出异常
     * @param operator
     * @param operand
     */
    private void checkNumberOperand(Token operator,Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator,"Operand must be a number");
    }

    /**
     * 检查数据是否满足条件，不满足抛出异常
     * @param operator
     * @param left
     * @param right
     */
    private void checkNumberOperands(Token operator,Object left,Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator,"Operands must be numbers.");
    }
    /**
     * 逻辑运算
     * false和nil为假，其余为真
     * @param object
     * @return
     */
    private boolean isTruthy(Object object) {
        if (object == null) return  false;
        //false或true
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    /**
     * 判断两个值是否相等
     * @param a
     * @param b
     * @return
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    /**
     * 将java的值转换为lox的
     * @param object
     * @return
     */
    private String stringify(Object object) {
        //为空的情况下返回nil
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            //整数的情况下返回整数
            if (text.endsWith(".0")) {
                text = text.substring(0,text.length()-2);
            }
            return text;
        }
        return object.toString();
    }

    private Object evaluate(Expr expr) {
        //括号内是什么类型的Expr就调用什么visit方法
        return expr.accept(this);
    }
}
