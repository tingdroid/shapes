/*
 * Assembler.java
 *
 * This file is part of GeomLab
 * Copyright (c) 2005 J. M. Spivey
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package funbase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import funbase.Machine.ByteCode;

/** This class provides a primitive that translates the list of instructions
 *  generated by the compiler into a ByteCode object. */
public class Assembler {
    private int size = 0, depth = 0, maxdepth = 0, maxframe = 0;

    private int ops[], rands[];
    private int ip = 0;
    private List<Value> consts = new ArrayList<Value>();

	/** Table showing the address of each label. */
	private Map<Integer, Integer> labdict = new HashMap<Integer, Integer>(10);

	/** Table showing the eval stack depth at each label */
	private Map<Integer, Integer> labdepth = new HashMap<Integer, Integer>(10);

    protected Assembler() { }

    /** Generate an instruction with an integer operand */
    private void gen(int op, int rand) {
	ops[ip] = op; rands[ip] = rand; ip++;
	depth += delta(op, rand);
	if (depth > maxdepth) maxdepth = depth;
    }

    /** Allocate slot in constant pool */
    private int constant(Value v) {
	int rand = consts.indexOf(v);
	
	if (rand < 0) {
	    rand = consts.size();
	    consts.add(v);
	}

	return rand;
    }

    /** Determine the change in stack depth caused by executing
     *  an instruction. */
    private int delta(int op, int rand) {
	switch (op) {
	    case Machine.CONST:
	    case Machine.INT:
	    case Machine.GLOBAL:
	    case Machine.LOCAL:
	    case Machine.ARG:
	    case Machine.FVAR:
		return 1; 

	    case Machine.JFALSE:
	    case Machine.BIND:
	    case Machine.GUARD:
	    case Machine.MCONST:
	    case Machine.MINT:
	    case Machine.POP: 
	    case Machine.RETURN: 
	    case Machine.TOPVAL: 
	    case Machine.TOPDEF: 
		return -1;

	    case Machine.JUMP:
	    case Machine.TRAP:
	    case Machine.MPLUS: 
		return 0; 

	    case Machine.MEQ: 
		return -2; 

 	    case Machine.CLOSURE: 
	    case Machine.CALL: 
	    case Machine.TCALL: 
		return -rand;

	    case Machine.LIST: 
		return -(rand-1);

	    case Machine.MLIST: 
		return rand-1;

	    case Machine.MPRIM: 
		return rand-2;

	    default:
		throw new Error("Assembler.delta");
	}
    }
    
    /** firstPass -- calculate size and label offsets */
    private void firstPass(Value code, ErrContext cxt) {
	for (Value xs = code; !xs.isNilValue(); xs = cxt.tail(xs)) {
	    Value inst = cxt.head(xs);

	    if (inst.isNumValue()) {
		int lab = (int) cxt.number(inst);
		labdict.put(lab, size);
	    }
	    else if (inst.isConsValue()) 
		size++;
	}
    }
    
    /** secondPass -- assemble the instructions */
    private void secondPass(Value code, ErrContext cxt) {
	for (Value xs = code; !xs.isNilValue(); xs = cxt.tail(xs)) {
	    Value inst = cxt.head(xs);

	    if (inst.isNumValue()) {
		/* A label.  We prefer to trust the stack depth
		 * recorded for the JUMP instruction that leads here,
		 * because the current stack depth may be wrong if
		 * execution cannot fall through from above. */
		int lab = (int) cxt.number(inst);
		Integer d = labdepth.get(lab);
		if (d != null) depth = d; 
	    }
	    else if (inst.isConsValue()) {
		int op = (int) cxt.number(cxt.head(inst));
		Value rands = cxt.tail(inst);
		switch (op) {
		    case Machine.POP:
		    case Machine.GUARD:
		    case Machine.MEQ:
		    case Machine.RETURN:
		    case Machine.TOPVAL:
			/* Instructions that have no operand */
			gen(op, 0);
			break;
			
		    case Machine.CONST:
		    case Machine.GLOBAL:
		    case Machine.MCONST:
		    case Machine.MPLUS:
		    case Machine.TOPDEF:
			/* Instructions with a value operand */
			gen(op, constant(cxt.head(rands)));
			break;

		    case Machine.ARG:
		    case Machine.LOCAL:
		    case Machine.FVAR:
		    case Machine.CLOSURE:
		    case Machine.MLIST:
		    case Machine.MPRIM:
		    case Machine.CALL:
		    case Machine.TCALL:
		    case Machine.LIST:
		    case Machine.INT:
		    case Machine.MINT:
			/* Instructions with an integer operand */
			gen(op, (int) cxt.number(cxt.head(rands)));
			break;

		    case Machine.JUMP:
		    case Machine.JFALSE:
		    case Machine.TRAP: {
			/* Instructions with a label operand. */
			int lab = (int) cxt.number(cxt.head(rands));
			gen(op, labdict.get(lab));
			labdepth.put(lab, depth);
			break;
		    }

		    case Machine.BIND: {
			/* Make sure integer operand is within the frame */
			int rand =(int) cxt.number(cxt.head(rands));
			gen(op, rand);
			if (rand >= maxframe) maxframe = rand+1;
			break;
		    }

		    default:
			cxt.expect("opcode");
		}
	    }
	    else
		cxt.expect("instruction");
	}
    }

    public ByteCode assemble(String name, int arity, 
			     Value code, ErrContext cxt) {
	firstPass(code, cxt);
	this.ops = new int[size]; this.rands = new int[size];
	secondPass(code, cxt);
	assert ip == size;
	return new Machine.ByteCode(name, arity, maxframe, maxdepth,
		ops, rands, consts.toArray(new Value[0]));
    }

    public static Primitive primitives[] = {
	new Primitive("assemble", 3) {
	    public Value invoke(Value args[], int base) {
		String f = args[base+0].toString();
		int arity = (int) cxt.number(args[base+1]);
		Value code = args[base+2];
		Assembler asm = new Assembler();
		return asm.assemble(f, arity, code, cxt);
	    }
	}
    };
}
