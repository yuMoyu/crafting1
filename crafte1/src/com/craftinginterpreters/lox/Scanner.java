package com.craftinginterpreters.lox;

import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * 扫描器，读取字符
 */
class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    //当前行指针
    private int current = 0;
    //行数
    private int line = 1;

    //关键字
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else" , ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil" ,NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);

    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        //循环扫描每一行代码
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }
        //在扫描尾部加上结束标记
        tokens.add(new Token(EOF,"",null,line));
        return tokens;
    }

    /**
     * 识别并转换词素
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':addToken(LEFT_PAREN);break;
            case ')':addToken(RIGHT_PAREN);break;
            case '{':addToken(LEFT_BRACE);break;
            case '}':addToken(RIGHT_BRACE);break;
            case ',':addToken(COMMA);break;
            case '.':addToken(DOT);break;
            case '-':addToken(MINUS);break;
            case '+':addToken(PLUS);break;
            case ';':addToken(SEMICOLON);break;
            case '*':addToken(STAR);break;
            case '!':
                //如果当前元素的下一元素是=，则匹配为!=，否则匹配为!
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL :EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                //如果是注释就把本行吃掉，一直到行尾都不解析
                if (match('/')) {
                    //peek和isAtEnd换一下位置，peek里面不就不用校验isAtEnd了吗？
                    //为啥不用match？
                    //当遇到空白字符时，我们只需回到扫描循环的开头。这样就会在空白字符之后开始一个新的词素。对于换行符，我们做同样的事情，但我们也会递增行计数器。(这就是为什么我们使用peek() 而不是match()来查找注释结尾的换行符。我们到这里希望能读取到换行符，这样我们就可以更新行数了)
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    //否则添加除法标记
                    addToken(SLASH);
                }
                break;
            //忽略空白字符
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                //遇到换行符的情况下，递增行计数器
                line++;
                break;
            case '"':string(); break;

            default:
                if (isDigit(c)) {
                    //识别数字词素
                    number();
                } else if (isAlpha(c)) {
                    //如果当前字符是字母，就执行identifier
                    identifier();
                }else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * 用最长匹配原则判断当前词素是关键字还是标识符
     */
    private void identifier() {
        //如果当前current所指是字母或数字，且未到结尾，指针就后移
        //比如abc123
        while (isAlphaNumeric(peek())) advance();
        //获取进入本次while循环的字符串
        String text = source.substring(start, current);
        //判断是否是关键字
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
    /**
     * 处理数字词素
     */
    private void number() {
        //如1234.5678
        //本while消费1234
        while (isDigit(peek())) advance();
        //当前字符是.，下一个字符也是数字进入本分支
        if (peek() == '.' && isDigit(peekNext())) {
            //本if分支消费.5678
            advance();
            while (isDigit(peek())) advance();
        }
        //将整个数字添加为单个词素
        addToken(NUMBER,Double.parseDouble(source.substring(start,current)));
    }

    /**
     * 批次字符串
     */

    private void string() {
        //消费字符，直到找到另一个"为止
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        //如果已经到结尾但，只有一个"代表不是完整的字符串，所以报错
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
    }
    /**
     * 判断元素是否与期望的一致
     * @param expected
     * @return
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        //指针后移
        current++;
        return true;
    }

    /**
     * 返回当前字符
     * @return
     */
    public char peek() {
        //如果指针已经移动到尾部，就返回0
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * 返回下一位字符
     * @return
     */
    private char peekNext() {
        if (current + 1 >source.length()) return '\0';
        return source.charAt(current+1);
    }

    /**
     * 是英文字母
     * @param c
     * @return
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * 是英文字母或数字
     * @param c
     * @return
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    /**
     * 当前字符是否为数字
     * @param c
     * @return
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    /**
     * 判断是否结束
     * @return
     */
    private boolean isAtEnd() {
        //当前指针=源码长度，代表指针已经移动到代码后一位，因为初始current=0
        return current >= source.length();
    }


    /**
     * 获取源码的当前字符，并将current指针移动到下一位
     * @return
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * 调用另一个addToken
     * @param type
     */
    private void addToken(TokenType type) {
        addToken(type,null);
    }

    /**
     * 将当前词素文本组装为一个token
     * @param type
     * @param literal
     */
    private void addToken(TokenType type,Object literal) {
        String text = source.substring(start,current);
        tokens.add(new Token(type, text,literal,line));
    }
}
