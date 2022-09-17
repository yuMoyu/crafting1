package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException{
        //如果是源码有多个地址就报错
        if (args.length > 1) {
            System.out.println("Usage:jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            //从命令行输入源码地址
            runFile(args[0]);
        } else {
            //交互式的执行代码
            runPrompt();
        }
    }
    private static void runFile(String path) throws IOException {
        //根据地址读取文件
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        //以默认编码格式执行
        run(new String(bytes, Charset.defaultCharset()));
        //如果发生错误，以非零的结束代码退出
        if (hadError)
            System.exit(65);
        //从文件中运行脚本发生错误时，退出
        if (hadRuntimeError) System.exit(70);
    }
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (; ; ) {
            System.out.print("> ");
            //当本地没有输入的时候跳出循环
            String line = reader.readLine();
            if (line == null) break;
            //一行行的运行代码
            run(line);
            //交互式循环，如果用户输入错误，不应该终止整个会话
            hadError = false;
        }
    }
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        //转换为语法标记
        List<Token> tokens = scanner.scanTokens();
        //利用解析树和语法树将语法标记进行组合
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();
        //如果存在语法错误就返回
        if (hadError) return;
        //调用解释器
        interpreter.interpret(expression);

    }

    static void error(int line, String message) {
        report(line,"",message);
    }
    private static void report(int line,String where,String message) {
        System.err.println("[line"+line+"]Error"+where+":"+message);
        hadError = true;
    }
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line,"at end",message);
        } else {
            report(token.line, " at '"+token.lexeme+"'",message);
        }
    }

    /**
     * 运行时异常
     * @param error
     */
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line "+ error.token.line +"]");
        hadRuntimeError = true;
    }
}
