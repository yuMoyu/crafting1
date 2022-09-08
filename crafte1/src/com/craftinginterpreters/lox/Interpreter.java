package com.craftinginterpreters.lox;

/**
 * 解释器-计算值
 */
class Interpreter implements Expr.Visitor<Object>{

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case GREATER:
                return (double)left > (double) right;
            case GREATER_EQUAL:
                return (double)left >= (double) right;
            case LESS:
                return (double)left < (double) right;
            case LESS_EQUAL:
                return (double)left <= (double) right;
            case MINUS:
                return (double)left - (double) right;
            case PLUS:
                if (left instanceof  Double && right instanceof Double) {
                    return (double)left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    //那如果是左边string右边数字呢，或者相反
                    return (String)left + (String)right;
                }
                break;
            case SLASH:
                return (double)left / (double) right;
            case STAR:
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
    private Object evaluate(Expr expr) {
        //括号内是什么类型的Expr就调用什么visit方法
        return expr.accept(this);
    }
}
