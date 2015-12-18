package org.ifollowyou.saber.xml;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;

/**
 * XML配置文件合法性校验器。
 *
 * @author xman
 */
public class XmlValidator {
    /**
     * 验证XML文件是否符合Schema文件指定规范。
     *
     * @param xsd schema文件
     * @param xml xml文件
     * @return 验证结果
     * @version
     */
    public static boolean validate(final File xsd, final File xml) {
        boolean valid = false;
        try {
            // Create SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            // Create Schema object
            Schema schema = factory.newSchema(xsd);
            // Create Validator and set ErrorHandler on Validator.
            Validator validator = schema.newValidator();
            ErrorHandlerImpl errorHandler = new ErrorHandlerImpl();
            validator.setErrorHandler(errorHandler);
            // Validate XML Document
            StreamSource streamSource = new StreamSource(xml);
            validator.validate(streamSource);
            // Output Validation Errors
            if (errorHandler.isValidationError()) {
                System.out.println(errorHandler.getSaxParseException().getMessage());
            } else {
                valid = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return valid;
    }

    /**
     * 校验异常信息输出处理类。
     */
    private static class ErrorHandlerImpl extends DefaultHandler {
        /**
         * The validation error.
         */
        private boolean validationError = false;

        /**
         * The sax parse exception.
         */
        private SAXParseException saxParseException = null;

        /**
         * Error.
         *
         * @param exception the exception
         * @throws SAXException the sAX exception
         * @version
         */
        public void error(SAXParseException exception) throws SAXException {
            validationError = true;
            saxParseException = exception;
        }

        /**
         * Fatal error.
         *
         * @param exception the exception
         * @throws SAXException the sAX exception
         * @version
         */
        public void fatalError(SAXParseException exception) throws SAXException {
            validationError = true;
            saxParseException = exception;
        }

        /**
         * Warning.
         *
         * @param exception the exception
         * @throws SAXException the sAX exception
         * @version
         */
        public void warning(SAXParseException exception) throws SAXException {
            validationError = true;
            saxParseException = exception;
        }

        /**
         * @return the validationError
         */
        public boolean isValidationError() {
            return validationError;
        }

        /**
         * @return the saxParseException
         */
        public SAXParseException getSaxParseException() {
            return saxParseException;
        }
    }
}
