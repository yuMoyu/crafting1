package com.craftinginterpreters.lox;

/**
 * 标记
 */
class Token {
    //字符串类型
    final TokenType type;
    //输入的字符串
    final String lexeme;
    //对应java中内容
    final  Object literal;
    //所在行，用来报错
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type+""+lexeme+" "+literal;
    }
}
