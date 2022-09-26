package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {

    final Environment enclosing;
    /**
     * key ��������
     * value ����ֵ
     */
    private final Map<String,Object> values = new HashMap<>();

    /**
     * ����ȫ��������
     */
    Environment() {
        enclosing = null;
    }

    /**
     * �����ⲿ�������ڵ��µľֲ�������
     * enclosing��Ϊ�ⲿ�����������values��Ϊ��ǰ���������
     * @param enclosing
     */
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * ��ȡ����ֵ
     * @param name
     * @return
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        //�����ǰ������û���ҵ�������ǰһ��if����������Χ������Ѱ�� ֱ�������������
        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme+"'.");
    }

    /**
     * ��ֵ�������ڴ��ڱ���������¶�����и�ֵ
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
     * �������� �������¶��壬�µĸ���ԭ�е�
     * @param name
     * @param value
     */
    void define(String name, Object value) {
        values.put(name,value);
    }

}
