:if exists("b:current_syntax")
:  finish
:endif

" keywords
:syntax keyword benderKeyword class fun val 
:syntax keyword benderKeyword for while return

" booleans
:syntax keyword benderBoolean true false

" constants
:syntax keyword benderConstant nada

" functions
:syntax keyword benderFunction print 

" operators
:syntax match benderOperator "\v\*"
:syntax match benderOperator "\v\+"
:syntax match benderOperator "\v\-"
:syntax match benderOperator "\v/"
:syntax match benderOperator "\v\="
:syntax match benderOperator "\v!"

" conditionals
:syntax keyword benderConditional if else and or else

" numbers
:syntax match benderNumber "\v\-?\d*(\.\d+)?"

" strings
:syntax region benderString start="\v\"" end="\v\""

" comments
:syntax match benderComment "\v//.*$"

:highlight link benderKeyword Keyword
:highlight link benderBoolean Boolean
:highlight link benderConstant Constant
:highlight link benderFunction Function
:highlight link benderOperator Operator
:highlight link benderConditional Conditional
:highlight link benderNumber Number
:highlight link benderString String
:highlight link benderComment Comment

:let b:current_syntax = "bender"

