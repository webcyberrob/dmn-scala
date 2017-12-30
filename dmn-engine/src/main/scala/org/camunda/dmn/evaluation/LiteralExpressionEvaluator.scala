package org.camunda.dmn.evaluation

import org.camunda.dmn.DmnEngine._
import org.camunda.feel._
import org.camunda.feel.interpreter.{RootContext, ValFunction}
import org.camunda.bpm.model.dmn.instance.{LiteralExpression, UnaryTests}
import scala.Left
import scala.Right

class LiteralExpressionEvaluator(feelEngine: FeelEngine) {
  
  def evalUnaryTests(unaryTests: UnaryTests, context: EvalContext): Either[Failure, Any] = 
    eval(unaryTests.getText.getTextContent, context)
  
  def evalExpression(literalExpression: LiteralExpression, context: EvalContext): Either[Failure, Any] = 
    eval(literalExpression.getText.getTextContent, context) 
  
  private def eval(expr: String, context: EvalContext): Either[Failure, Any] = {
    
    val expression = context.parsedExpressions(expr)
    
    val functions = context.variables
      .filter{ case (k,v) => v.isInstanceOf[ValFunction]}
      .map{ case (k,f) => k -> List(f.asInstanceOf[ValFunction])}
    
    val evalContext = RootContext(
      variables = context.variables,
      additionalFunctions = functions)
    
    feelEngine.eval(expression, evalContext) match {
        case EvalValue(value) => Right(value)
        case EvalFailure(msg) => Left(Failure(msg))
        case ParseFailure(msg) => Left(Failure(msg))
    }
  }
  
}