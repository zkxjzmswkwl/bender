" Syntax highlighting for the Bender language
syntax clear

" Comments
syntax match benderComment "\/\/.*" contained
highlight link benderComment Comment

" Keywords
syntax keyword benderKeyword if else while for return and or nil true false
highlight link benderKeyword Keyword

" Constants and built-in functions
syntax keyword benderConstant nil true false
highlight link benderConstant Constant

syntax keyword benderBuiltin print fuckit capture
highlight link benderBuiltin Function

" Function and class definitions
syntax match benderFunction "fun\s\+\w\+" nextgroup=benderFunctionName skipwhite
syntax match benderFunctionName "\w\+\s*(" contained
highlight link benderFunction Keyword
highlight link benderFunctionName Function

syntax match benderClass "class\s\+\w\+" nextgroup=benderClassName skipwhite
syntax match benderClassName "\w\+\s*{" contained
highlight link benderClass Keyword
highlight link benderClassName Type

" Variables and parameters
syntax match benderVariable "\w\+" contained
highlight link benderVariable Identifier

" Strings and Numbers
syntax match benderString "\".*\"" contained
syntax match benderNumber "\v\d+(\.\d+)?"
highlight link benderString String
highlight link benderNumber Number

" Define the default highlighting
if version >= 508 || !exists("did_bender_syntax_inits")
    if version < 508
        let did_bender_syntax_inits = 1
        command -nargs=+ HiLink hi link <args>
    else
        command -nargs=+ HiLink hi def link <args>
    endif

    HiLink benderComment Comment
    HiLink benderKeyword Keyword
    HiLink benderConstant Constant
    HiLink benderBuiltin Function
    HiLink benderFunction Keyword
    HiLink benderFunctionName Function
    HiLink benderClass Keyword
    HiLink benderClassName Type
    HiLink benderVariable Identifier
    HiLink benderString String
    HiLink benderNumber Number

    delcommand HiLink
endif

let b:current_syntax = "bender"
