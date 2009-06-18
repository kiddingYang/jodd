// Copyright (c) 2003-2009, Jodd Team (jodd.org). All Rights Reserved.

package jodd.proxetta.data;

import jodd.madvoc.meta.Action;

public class BigFatJoe extends SmallSkinnyZoe {

	static {
		System.out.println("BigFatJoe.static intializer");
	}

	static {
		StatCounter.counter++;
	}

	{
		StatCounter.counter++;
	}

	@Action(method = "method", extension = "extension", alias = "alias", notInPath = true, value = "value")
	public void publicMethod() {
		System.out.println("BigFatJoe.publicMethod");
	}

	public void callInnerMethods() {
		System.out.println("BigFatJoe.callInner");
		protectedMethod();
		packageMethod();
		privateMethod();
	}

	public void callInnerMethods2() {
		System.out.println("BigFatJoe.callInnerMethods2");
		superProtectedMethod();
		superPackageMethod();
	}

	protected void protectedMethod() {
		System.out.println("BigFatJoe.protectedMethod");
	}

	void packageMethod() {
		System.out.println("BigFatJoe.packageMethod");
	}

	private void privateMethod() {
		System.out.println("BigFatJoe.privateMethod");
	}

}
