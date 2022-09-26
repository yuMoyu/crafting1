package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {

    final Environment enclosing;
    /**
     * key 变量名称
     * value 变量值
     */
    private final Map<String,Object> values = new HashMap<>();

    /**
     * 用于全局作用域
     */
    Environment() {
        enclosing = null;
    }

    /**
     * 用于外部作用域内的新的局部作用域
     * enclosing视为外部作用域变量，values视为当前作用域变量
     * @param enclosing
     */
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * 获取变量值
     * @param name
     * @return
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        //如果当前环境中没有找到变量（前一个if），就在外围环境中寻找 直到不存在最外层
        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme+"'.");
    }

    /**
     * 赋值操作，在存在变量的情况下对其进行赋值
     * @param name
     * @param value
     */
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '"  + name.lexeme +"'.");
    }

    /**
     * 变量定义 允许重新定义，新的覆盖原有的
     * @param name
     * @param value
     */
    void define(String name, Object value) {
        values.put(name,value);
    }

}
