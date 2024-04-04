#ifndef bender_value_h
#define bender_value_h

#include "common.h"

typedef double Value;

#define IS_BOOL(value)    (((value) == TRUE_VAL || (value) == FALSE_VAL))

#define IS_NUMBER(value)  (((value) & NAN_TAG) != TAG_NIL)
#define IS_NIL(value)     ((value) == NIL_VAL)
#define IS_OBJ(value)     (((value) & NAN_TAG) == TAG_OBJ)

#define AS_BOOL(value)    ((value) == TRUE_VAL)
#define AS_NUMBER(value)  value
#define AS_OBJ(value)     ((Obj*)(uintptr_t)((value) & ~(NAN_TAG | SIGN_BIT)))

#define BOOL_VAL(b)       ((b) ? TRUE_VAL : FALSE_VAL)
#define NIL_VAL           ((Value)(uint64_t)(NAN_TAG | TAG_NIL))
#define NUMBER_VAL(num)   ((Value)(num))
#define OBJ_VAL(obj)      ((Value)(SIGN_BIT | NAN_TAG | (uint64_t)(uintptr_t)(obj))

typedef struct Obj Obj;

typedef struct {
    int count;
    int capacity;
    Value* values;
} ValueArray;

void init_value_array(ValueArray* array);
void write_value_array(ValueArray* array, Value value);
void free_value_array(ValueArray* array);
void print_value(Value value);


#endif