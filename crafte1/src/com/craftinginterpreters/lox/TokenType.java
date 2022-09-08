package com.craftinginterpreters.lox;

//标记类型
enum TokenType {
    //运算符
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    //SEMICOLON;
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    //标记符号位
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    //文字类型
    IDENTIFIER, STRING, NUMBER,

    //关键字
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}
