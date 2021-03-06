/**
 * Copyright (C) 2011-2015 Incapture Technologies LLC
 *
 * This is an autogenerated license statement. When copyright notices appear below
 * this one that copyright supercedes this statement.
 *
 * Unless required by applicable law or agreed to in writing, software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * Unless explicit permission obtained in writing this software cannot be distributed.
 */
package rapture.repo.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import rapture.repo.jdbc.context.ConverterContext;
import rapture.repo.jdbc.context.StatementType;

/**
 * Created by yanwang on 4/28/15.
 */
public class PreparedExpressionConverter extends ExpressionDeParser {
    private static Logger log = Logger.getLogger(PreparedExpressionConverter.class);

    private List<? super Object> values = new ArrayList<>();
    private ConverterContext context;

    public PreparedExpressionConverter(SelectVisitor selectVisitor, StringBuilder buffer, ConverterContext context) {
        super(selectVisitor, buffer);
        this.context = context;
    }

    public ConverterContext getContext() {
        return context;
    }

    public List<? super Object> getValues() {
        return values;
    }

    @Override
    public void visit(SubSelect subSelect) {
        context.increaseContextLevel(StatementType.SELECT);
        super.visit(subSelect);
        context.decreaseContextLevel();
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        context.setNegative('-' == signedExpression.getSign());
        signedExpression.getExpression().accept(this);
        context.setNegative(false);
    }

    @Override
    public void visit(DateValue dateValue) {
        this.getBuffer().append("?");
        values.add(dateValue.getValue());
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        this.getBuffer().append("?");
        double value = doubleValue.getValue();
        if(context.isNegative()) {
            value = -value;
        }
        values.add(value);
    }

    @Override
    public void visit(LongValue longValue) {
        this.getBuffer().append("?");
        long value = longValue.getValue();
        if(context.isNegative()) {
            value = -value;
        }
        values.add(value);
    }

    @Override
    public void visit(StringValue stringValue) {
        this.getBuffer().append("?");
        values.add(stringValue.getValue());
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        this.getBuffer().append("?");
        values.add(timestampValue.getValue());
    }

    @Override
    public void visit(TimeValue timeValue) {
        this.getBuffer().append("?");
        values.add(timeValue.getValue());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("buffer = ").append(getBuffer()).append("\n");
        sb.append("values = ").append(values).append("\n");
        return sb.toString();
    }
}
