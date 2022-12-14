package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * 运用递归向下的方法写解释器，读取语法标记
 * 解析树
 * expression     → assignment ;
 * assignment     → IDENTIFIER "=" assignment | equality ;
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           → factor ( ( "-" | "+" ) factor )* ;
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 * unary          → ( "!" | "-" ) unary
 *                | primary ;
 * primary        → NUMBER | STRING | "true" | "false" | "nil"
 *                | "(" expression ")" ;
 */
class Parser {
    private static class ParserError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * 初始方法启动解析器
     * @return
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }


    /**
     * 此规则用于扩展Lox语法以支持语句
     * program        → declaration* EOF ;
     *
     * declaration    → varDecl //变量
     *                | statement ;
     *
     * statement      → exprStmt
     *                | printStmt
     *                | block;
     * block          → "{" declaration* "}" ;
     *
     * exprStmt       → expression ";" ;  如：true;
     * printStmt      → "print" expression ";" ; 如：print true;
     */


    /**
     * @return
     */
    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParserError error) {
            synchronize();
            return null;
        }
    }

    /**
     * 返回stmt语句，带print就算print语句，否则算表达式
     * @return
     */
    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        //如果发现带左括号，就返回block语句
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    /**
     * 将print语句组装为stmt类型的print
     * @return
     */
    private Stmt printStatement() {
        Expr value = expression();
        //如果当前语句以分号结尾就是正常
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }
    private Stmt varDeclaration() {
        //name为标识符 如var a = b; name为a
        Token name = consume(IDENTIFIER,"Expect variable name.");
        Expr initializer = null;
        //如果下个字符是=
        if (match(EQUAL)) {
            //initializer为等号后面的语句 ,initializer为b
            initializer = expression();
        }
        consume(SEMICOLON,"Expect ';' after variable declaration.");
        return new Stmt.Var(name,initializer);
    }
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /**
     * 块语法
     * @return
     */
    private List<Stmt> block() {
        //创建空列表
        List<Stmt> statements = new ArrayList<>();
        //将块内语句都放入列表
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        //找到 } 正常结束
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }
    /**
     * 赋值表达式，在存在变量的情况下，对其进行二次赋值
     * @return
     */
    private Expr assignment() {
        Expr expr = equality();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }
    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL,EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG,MINUS)) {
            Token operatpr = previous();
            Expr right = unary();
            return new Expr.Unary(operatpr, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        //如果均不匹配：不是表达式开头的语法标记，就报错
        throw error(peek(),"Expect expression.");
    }
    /**
     * 判断当前标记是否为给定类型。是则消费标记并返回true，否则返回false并保留标记。
     * @param types
     * @return
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * 判断下一个标记是否是预期类型。是则消费标记，否则报错。（恐慌模式：出现错误，就跳出到最顶层，停止解析）
     * @param type
     * @param message
     * @return
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(),message);
    }
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * 消费当前标记
     * @return
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * 判断是否处理完了待解析标记
     * @return
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * 返回还未消费的当前标记
     * @return
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * 返回最近消费的标记
     * @return
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * 返回异常
     * @param token
     * @param message
     * @return
     */
    private ParserError error(Token token, String message) {
        Lox.error(token, message);
        return new ParserError();
    }

    /**
     * 找到下一条语句的开头
     */
    private void synchronize() {
        //消费一个标记
        advance();
        //如果当前未结束
        while (!isAtEnd()) {
            //如果上一个标记类型为;就返回
            if (previous().type == SEMICOLON) return;
            //如果当前字符类型是开头的关键字就返回
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                    return;
            }
            //否则继续消费标记
            advance();
        }
    }
}
