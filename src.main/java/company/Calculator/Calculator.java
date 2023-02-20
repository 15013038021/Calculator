package company.Calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
/**
 * 实现了一个基本的简单的加减乘除计算器， 同时支持 redo 与 undo 操作
 * 比如：5+1+2=8, undo 一次后为6, redo 一次后为8
 **/
class Calculator {

    private Double preRes; // 上一次的计算值
    private Double newNum; // 新加的操作数
    private String curOperator; // 当前操作符
    private List<Double> numList = new ArrayList<>(); //存储一波运算的操作数
    private List<String> optList = new ArrayList<>(); // 存储一波运算的运算符
    private List<Double> resList = new ArrayList<>(); // 存储一波运算后的系列值

    // 记录 undo/redo 操作到历史计算值的哪个地方，相当于一个索引
    // 第一次执行undo 时 lastOptIndex 会 为optList 数组的长度，
    // 每进行一次 undo， lastOptIndex会减一， 相应的redo 时 lastOptIndex会加一
    private int lastOptIndex ;

    // 记录 undo/redo 有效索引最大值，比如当 undo 3次时， 这时将undo 打断后， 执行一次运算操作
    // 那么此时这次运算所在的索引位置将是undo/redo 的索引边界值
    private int validIndexMax;

    private int scale = 2; // 设置精度

    public Calculator(int scale ){
        this.scale = scale;
        this.lastOptIndex = optList.size()-1;
        this.validIndexMax = optList.size()-1;
    }


    /**
     * 设置操作数
     **/
    public void setOperationValue(Double newNum) {
        // 未计算过,累计总值为第一个输入值
        if (preRes == null) {
            preRes = newNum;
        } else {
            this.newNum = newNum;
        }
        System.out.println("当前输入的操作数为: "+ newNum);
    }

    /**
     * 设置运算符
     **/
    public void setCurOperator(String curOperator) {
        this.curOperator = curOperator;
        System.out.println("当前输入的运算符为: "+ curOperator);
    }

    /**
     * 执行 等于时，计算出结果；并更新相应的索引
     **/
    public void calc() {
        preRes = preRes == null ? 0 : preRes;
        if (curOperator == null) {
            System.out.println("请选择执行哪一种运算");
        }
        if (newNum == null){
            System.out.println("请输入操作数值");
        }

        if (newNum != null) {
            // 累加计算
            Double curRes = calcTwoNum(preRes, curOperator, newNum);
            System.out.println("当前: " +preRes+curOperator+newNum+" 的计算结果为: "+ curRes);
            if (lastOptIndex== resList.size()-1) { // 表示未有redo/undo 操作
                resList.add(curRes);
                numList.add(newNum);
                optList.add(curOperator);
                lastOptIndex++;
                validIndexMax++;
            } else { // 属于处于redo/undo中间过程种，执行了一次运算操作,需要将此次运算相关的值更新到对应的列表位置，并设置undo/redo validIndexMax索引的临界值
                lastOptIndex++;
                validIndexMax = lastOptIndex;
                resList.set(validIndexMax, curRes);
                numList.set(validIndexMax - 1, newNum);
                optList.set(validIndexMax - 1, curOperator);
            }
            preRes = curRes;
            curOperator = null;
            newNum = null;
        }
    }


    /**
     * 执行redo 操作
     **/
    public void undo() {

        if (resList.size() == 0) {
            System.out.println("当前无任何运算操作, 无法执行 undo!");
        } else if (resList.size() == 1) {
            System.out.println("执行 undo 操作后为: 0," + "undo 之前的值为:" + preRes);
            preRes = 0.0;
        } else {
            if (lastOptIndex - 1 < 0) {
                System.out.println("无法再undo!");
                return;
            }
            lastOptIndex--;
        }
        System.out.println("执行 undo 操作后的值:" + resList.get(lastOptIndex) + ", 执行 undo 操作前的值:" + preRes);
        preRes = resList.get(lastOptIndex);
        curOperator = null;
        newNum = null;
    }


    /**
     * 执行redo 操作
     **/
    public void redo() {
        try {
            if (lastOptIndex > -1) {
                if (lastOptIndex == resList.size()-1 || lastOptIndex == this.validIndexMax) {
                    System.out.println("无法再redo!");
                    return;
                }
                lastOptIndex++;
                System.out.println("执行 redo 操作后的值:" + resList.get(lastOptIndex) + ", 执行 redo 操作前的值:" + preRes);
                preRes = resList.get(lastOptIndex);
                curOperator = null;
                newNum = null;
            }
        } catch (Exception e) {
            System.out.println("redo异常,lastOptIndex:" + lastOptIndex);
        }
    }

    /**
     清除操作
     **/
    public void clear() {
        preRes = null;
        newNum = null;
        curOperator = null;
        numList = new ArrayList<>();
        optList = new ArrayList<>();
        resList = new ArrayList<>();
        lastOptIndex = -1;
        validIndexMax = -1;
    }

    /**
     * 执行 运算
     *
     * @param preRes    前面已累计值
     * @param curOperator 当前操作
     * @param newNum      新输入值
     * @return 计算结果
     */
    private Double calcTwoNum(Double preRes, String curOperator, Double newNum) {
        BigDecimal res = BigDecimal.ZERO;
        BigDecimal preResult = new BigDecimal(preRes);
        BigDecimal opeNum= new BigDecimal(newNum);
        curOperator = curOperator == null ? "+" : curOperator;
        switch (curOperator) {
            case "+":
                res = preResult.add(opeNum);
                break;
            case "-":
                res = preResult.subtract(opeNum).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "*":
                res = preResult.multiply(opeNum).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "/":
                res = preResult.divide(opeNum, RoundingMode.HALF_UP);
                break;
        }
        return res.doubleValue();
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator(2);
        // 加法
        calculator.setOperationValue(3.1);
        calculator.setCurOperator("+");
        calculator.setOperationValue(2.0);
        calculator.calc();
        calculator.clear();
        System.out.println();

        // 减法
        calculator.setOperationValue(7.1);
        calculator.setCurOperator("-");
        calculator.setOperationValue(2.0);
        calculator.calc();
        calculator.clear();
        System.out.println();

        //  乘法
        calculator.setOperationValue(7.1);
        calculator.setCurOperator("*");
        calculator.setOperationValue(2.0);
        calculator.calc();
        calculator.clear();
        System.out.println();

        //  除法
        calculator.setOperationValue(7.1);
        calculator.setCurOperator("/");
        calculator.setOperationValue(2.0);
        calculator.calc();
        calculator.clear();
        System.out.println();

        // undo/redo
        calculator.setOperationValue(13.0);
        calculator.setCurOperator("+");
        calculator.setOperationValue(2.0);
        calculator.calc();
        calculator.setCurOperator("+");
        calculator.setOperationValue(3.0);
        calculator.calc();
        calculator.setCurOperator("+");
        calculator.setOperationValue(1.0);
        calculator.calc();
        calculator.setCurOperator("+");
        calculator.setOperationValue(1.0);
        calculator.calc();
        calculator.undo();
        calculator.undo();
        calculator.undo();
        calculator.redo();
        calculator.setCurOperator("*");
        calculator.setOperationValue(3.0);
        calculator.calc();
        calculator.undo();
        calculator.redo();
    }
}

