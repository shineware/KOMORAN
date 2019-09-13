package kr.co.shineware.nlp.komoran.admin.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomH2Dialect extends H2Dialect {
    private static final Logger logger = LoggerFactory.getLogger(CustomH2Dialect.class);

    public CustomH2Dialect() {
        super();
        this.registerFunction("GROUP_CONCAT", new StandardSQLFunction("group_concat", StringType.INSTANCE));
        this.registerFunction("CONCAT", new StandardSQLFunction("concat", StringType.INSTANCE));
        logger.info("CustomFunctions have been registered");
    }
}
