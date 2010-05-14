/*
 * Copyright (c) 2007 Tom Parker <thpr@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package plugin.lsttokens.testsupport;

import java.net.URISyntaxException;

import org.junit.Test;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.ChoiceSet;
import pcgen.cdom.base.PersistentTransitionChoice;
import pcgen.cdom.base.PrimitiveChoiceSet;
import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.reference.ReferenceManufacturer;
import pcgen.persistence.PersistenceLayerException;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.CDOMSecondaryToken;
import pcgen.rules.persistence.token.QualifierToken;
import plugin.qualifier.pobject.AnyToken;
import plugin.qualifier.pobject.QualifiedToken;

public abstract class AbstractChooseTokenTestCase<T extends CDOMObject, TC extends CDOMObject>
		extends AbstractTokenTestCase<T>
{

	private static QualifierToken<CDOMObject> qual = new QualifiedToken<CDOMObject>();

	private static QualifierToken<CDOMObject> anyqualifier = new AnyToken<CDOMObject>();

	public abstract CDOMSecondaryToken<?> getSubToken();

	private static boolean allowsPCQualifier;

	public String getSubTokenName()
	{
		return getSubToken().getTokenName();
	}

	public abstract Class<TC> getTargetClass();

	@Override
	public void setUp() throws PersistenceLayerException, URISyntaxException
	{
		super.setUp();
		QualifierToken<? extends CDOMObject> pcqual = getPCQualifier();
		allowsPCQualifier = pcqual != null;
		TokenRegistration.register(getSubToken());
		TokenRegistration.register(qual);
		TokenRegistration.register(anyqualifier);
		if (allowsPCQualifier)
		{
			TokenRegistration.register(pcqual);
		}
	}

	protected void construct(LoadContext loadContext, String one)
	{
		construct(loadContext, getTargetClass(), one);
	}

	protected void construct(LoadContext loadContext,
			Class<? extends CDOMObject> cl, String one)
	{
		loadContext.ref.constructCDOMObject(cl, one);
	}

	@Override
	protected String getAlternateLegalValue()
	{
		return getSubTokenName() + '|' + "TestWP1|TestWP2|TestWP3";
	}

	@Override
	protected String getLegalValue()
	{
		return getSubTokenName() + '|' + "TestWP1|TestWP2";
	}

	@Override
	protected ConsolidationRule getConsolidationRule()
	{
		return ConsolidationRule.OVERWRITE;
	}

	protected abstract boolean allowsQualifier();

	protected abstract boolean isTypeLegal();

	protected abstract boolean isAllLegal();

	protected abstract QualifierToken<? extends CDOMObject> getPCQualifier();

	@Test
	public void testInvalidInputEmptyString() throws PersistenceLayerException
	{
		assertFalse(parse(""));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputOnlySubToken() throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName()));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputOnlySubTokenPipe()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|'));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputJoinOnly() throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + "|,"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputString() throws PersistenceLayerException
	{
		assertEquals(!requiresLiteral(), parse(getSubTokenName() + '|'
				+ "String"));
		if (requiresLiteral())
		{
			assertNoSideEffects();
		}
		else
		{
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	protected boolean requiresLiteral()
	{
		return false;
	}

	@Test
	public void testInvalidInputJoinedDot() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		construct(primaryContext, "TestWP2");
		assertEquals(!requiresLiteral(), parse(getSubTokenName() + '|'
				+ "TestWP1.TestWP2"));
		if (requiresLiteral())
		{
			assertNoSideEffects();
		}
		else
		{
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidInputTypeEmpty() throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "TYPE="));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputNotTypeEmpty() throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "!TYPE="));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputTypeUnterminated()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "TYPE=One."));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputNotTypeUnterminated()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "!TYPE=One."));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputTypeDoubleSeparator()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "TYPE=One..Two"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputNotTypeDoubleSeparator()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "!TYPE=One..Two"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputTypeFalseStart()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "TYPE=.One"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputNotTypeFalseStart()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "!TYPE=.One"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidListEndPipe() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		assertFalse(parse(getSubTokenName() + '|' + "TestWP1|"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidListEndComma() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		assertFalse(parse(getSubTokenName() + '|' + "TestWP1,"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidListStartPipe() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		assertFalse(parse(getSubTokenName() + '|' + "|TestWP1"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidListStartComma() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		assertFalse(parse(getSubTokenName() + '|' + ",TestWP1"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidListDoubleJoinPipe()
			throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		construct(primaryContext, "TestWP2");
		assertFalse(parse(getSubTokenName() + '|' + "TestWP2||TestWP1"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidListDoubleJoinComma()
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + "TYPE=Foo,,!TYPE=Bar"));
		assertNoSideEffects();
	}

	@Test
	public void testInvalidInputNotBuilt() throws PersistenceLayerException
	{
		// Explicitly do NOT build TestWP2
		construct(primaryContext, "TestWP1");
		assertEquals(!requiresLiteral(), parse(getSubTokenName() + '|'
				+ "TestWP1|TestWP2"));
		if (requiresLiteral())
		{
			assertNoSideEffects();
		}
		else
		{
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidInputCheckTypeEqualLengthBar()
			throws PersistenceLayerException
	{
		/*
		 * Explicitly do NOT build TestWP2 (this checks that the TYPE= doesn't
		 * consume the |
		 */
		construct(primaryContext, "TestWP1");
		assertEquals(!requiresLiteral(), parse(getSubTokenName() + '|'
				+ "TestWP1|TYPE=TestType|TestWP2"));
		if (requiresLiteral())
		{
			assertNoSideEffects();
		}
		else
		{
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidInputCheckTypeDotLengthPipe()
			throws PersistenceLayerException
	{
		/*
		 * Explicitly do NOT build TestWP2 (this checks that the TYPE= doesn't
		 * consume the |
		 */
		construct(primaryContext, "TestWP1");
		assertEquals(!requiresLiteral(), parse(getSubTokenName() + '|'
				+ "TestWP1|" + "TYPE.TestType.OtherTestType|TestWP2"));
		if (requiresLiteral())
		{
			assertNoSideEffects();
		}
		else
		{
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testValidInputs() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		construct(primaryContext, "TestWP2");
		assertTrue(parse(getSubTokenName() + '|' + "TestWP1"));
		assertTrue(primaryContext.ref.validate(null));
		assertTrue(parse(getSubTokenName() + '|' + "TestWP1|TestWP2"));
		assertTrue(primaryContext.ref.validate(null));
		if (isTypeLegal())
		{
			assertTrue(parse(getSubTokenName() + '|' + "TYPE=TestType"));
			assertTrue(primaryContext.ref.validate(null));
			assertTrue(parse(getSubTokenName() + '|' + "TYPE.TestType"));
			assertTrue(primaryContext.ref.validate(null));
			assertTrue(parse(getSubTokenName() + '|'
					+ "TestWP1|TestWP2|TYPE=TestType"));
			assertTrue(primaryContext.ref.validate(null));
			assertTrue(parse(getSubTokenName() + '|'
					+ "TestWP1|TestWP2|TYPE=TestType.OtherTestType"));
			assertTrue(primaryContext.ref.validate(null));
		}
		if (isAllLegal())
		{
			assertTrue(parse(getSubTokenName() + "|ALL"));
			assertTrue(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testRoundRobinOne() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		construct(secondaryContext, "TestWP1");
		runRoundRobin(getSubTokenName() + '|' + "TestWP1");
	}

	@Test
	public void testRoundRobinParen() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1 (Test)");
		construct(secondaryContext, "TestWP1 (Test)");
		runRoundRobin(getSubTokenName() + '|' + "TestWP1 (Test)");
	}

	@Test
	public void testRoundRobinThree() throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		construct(primaryContext, "TestWP2");
		construct(primaryContext, "TestWP3");
		construct(secondaryContext, "TestWP1");
		construct(secondaryContext, "TestWP2");
		construct(secondaryContext, "TestWP3");
		runRoundRobin(getSubTokenName() + '|' + "TestWP1|TestWP2|TestWP3");
	}

	@Test
	public void testRoundRobinWithEqualType() throws PersistenceLayerException
	{
		if (isTypeLegal())
		{
			construct(primaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP1");
			construct(secondaryContext, "TestWP2");
			runRoundRobin(getSubTokenName() + '|'
					+ "TestWP1|TestWP2|TYPE=OtherTestType|TYPE=TestType");
		}
	}

	@Test
	public void testRoundRobinTestEquals() throws PersistenceLayerException
	{
		if (isTypeLegal())
		{
			runRoundRobin(getSubTokenName() + '|' + "TYPE=TestType");
		}
	}

	@Test
	public void testRoundRobinTestEqualThree() throws PersistenceLayerException
	{
		if (isTypeLegal())
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "TYPE=TestAltType.TestThirdType.TestType");
		}
	}

	@Test
	public void testInvalidInputAnyItem() throws PersistenceLayerException
	{
		if (isAllLegal())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + "|ALL|TestWP1"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputItemAny() throws PersistenceLayerException
	{
		if (isAllLegal())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|' + "TestWP1|ALL"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputAnyType() throws PersistenceLayerException
	{
		if (isTypeLegal())
		{
			assertFalse(parse(getSubTokenName() + '|' + "ALL|TYPE=TestType"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputTypeAny() throws PersistenceLayerException
	{
		if (isTypeLegal())
		{
			assertFalse(parse(getSubTokenName() + '|' + "TYPE=TestType|ALL"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInputInvalidAddsTypeNoSideEffect()
			throws PersistenceLayerException
	{
		if (isTypeLegal())
		{
			construct(primaryContext, "TestWP1");
			construct(secondaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP2");
			construct(primaryContext, "TestWP3");
			construct(secondaryContext, "TestWP3");
			assertTrue(parse(getSubTokenName() + '|' + "TestWP1|TestWP2"));
			assertTrue(parseSecondary(getSubTokenName() + '|'
					+ "TestWP1|TestWP2"));
			assertFalse(parse(getSubTokenName() + '|' + "TestWP3|TYPE="));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInputInvalidAddsBasicNoSideEffect()
			throws PersistenceLayerException
	{
		construct(primaryContext, "TestWP1");
		construct(secondaryContext, "TestWP1");
		construct(primaryContext, "TestWP2");
		construct(secondaryContext, "TestWP2");
		construct(primaryContext, "TestWP3");
		construct(secondaryContext, "TestWP3");
		construct(primaryContext, "TestWP4");
		construct(secondaryContext, "TestWP4");
		assertTrue(parse(getSubTokenName() + '|' + "TestWP1|TestWP2"));
		assertTrue(parseSecondary(getSubTokenName() + '|' + "TestWP1|TestWP2"));
		assertFalse(parse(getSubTokenName() + '|' + "TestWP3||TestWP4"));
		assertNoSideEffects();
	}

	@Test
	public void testInputInvalidAddsAllNoSideEffect()
			throws PersistenceLayerException
	{
		if (isAllLegal())
		{

			construct(primaryContext, "TestWP1");
			construct(secondaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP2");
			construct(primaryContext, "TestWP3");
			construct(secondaryContext, "TestWP3");
			assertTrue(parse(getSubTokenName() + '|' + "TestWP1|TestWP2"));
			assertTrue(parseSecondary(getSubTokenName() + '|'
					+ "TestWP1|TestWP2"));
			assertFalse(parse(getSubTokenName() + '|' + "TestWP3|ALL"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testRoundRobinTestAll() throws PersistenceLayerException
	{
		if (isAllLegal())
		{
			runRoundRobin(getSubTokenName() + "|ALL");
		}
	}

	@Test
	public void testInvalidInputJoinedDotQualifier()
			throws PersistenceLayerException
	{
		assertEquals(!requiresLiteral(), parse(getSubTokenName() + '|'
				+ "PC.QUALIFIED"));
		if (requiresLiteral())
		{
			assertNoSideEffects();
		}
		else
		{
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidInputQualifierOpenBracket()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED["));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifierCloseBracket()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifierEmptyBracket()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifierQualifier()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertTrue(parse(getSubTokenName() + '|' + "QUALIFIED[QUALIFIED]"));
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidInputJoinQualifiedOnlyPipe()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + "|QUALIFIED[|]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputJoinQualifiedOnlyComma()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + "|QUALIFIED[,]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputStringQualified()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertTrue(parse(getSubTokenName() + '|' + "QUALIFIED[String]"));
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidInputJoinedDotQualified()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			assertTrue(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1.TestWP2]"));
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidInputQualifiedTypeEmpty()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[TYPE=]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifiedNotTypeEmpty()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[!TYPE=]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifiedTypeUnterminated()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[TYPE=One.]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifiedNotTypeUnterminated()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[!TYPE=One.]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifiedTypeDoubleSeparator()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TYPE=One..Two]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifiedNotTypeDoubleSeparator()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[!TYPE=One..Two]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifiedTypeFalseStart()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[TYPE=.One]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidInputQualifiedNotTypeFalseStart()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[!TYPE=.One]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedListEndPipe()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[TestWP1|]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedListEndComma()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[TestWP1,]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedListStartPipe()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[|TestWP1]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedListStartComma()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|' + "QUALIFIED[,TestWP1]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedListDoubleJoinPipe()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP2||TestWP1]]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedListDoubleJoinComma()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TYPE=Foo,,!TYPE=Bar]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedInputNotBuilt()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			// Explicitly do NOT build TestWP2
			construct(primaryContext, "TestWP1");
			assertTrue(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2]"));
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidQualifiedDanglingType()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1]TYPE=Foo"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedDanglingPrimitive()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TYPE=Foo]TestWP1"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedDanglingTypePipe()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1]TYPE=Foo|TYPE=Bar"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedDanglingPrimitiveComma()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TYPE=Foo]TestWP1,TYPE=Bar"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testValidQualifiedInputLotsOr()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName()
					+ '|'
					+ "QUALIFIED[TYPE=Bar|TYPE=Goo]|QUALIFIED[TYPE=Foo|TYPE=Yea]");
		}
	}

	@Test
	public void testValidQualifiedInputLotsAnd()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName()
					+ '|'
					+ "QUALIFIED[TYPE=Bar,TYPE=Goo],QUALIFIED[TYPE=Foo,TYPE=Yea]");
		}
	}

	@Test
	public void testInvalidQualifiedInputCheckTypeEqualLengthBar()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			/*
			 * Explicitly do NOT build TestWP2 (this checks that the TYPE=
			 * doesn't consume the |
			 */
			construct(primaryContext, "TestWP1");
			assertTrue(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TYPE=TestType|TestWP2]"));
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testInvalidQualifiedInputCheckTypeDotLengthPipe()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			/*
			 * Explicitly do NOT build TestWP2 (this checks that the TYPE=
			 * doesn't consume the |
			 */
			construct(primaryContext, "TestWP1");
			assertTrue(parse(getSubTokenName() + '|' + "QUALIFIED[TestWP1|"
					+ "TYPE.TestType.OtherTestType|TestWP2]"));
			assertFalse(primaryContext.ref.validate(null));
		}
	}

	@Test
	public void testRoundRobinQualifiedOne() throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(secondaryContext, "TestWP1");
			runRoundRobin(getSubTokenName() + '|' + "QUALIFIED[TestWP1]");
		}
	}

	@Test
	public void testRoundRobinQualifiedParen() throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1 (Test)");
			construct(secondaryContext, "TestWP1 (Test)");
			runRoundRobin(getSubTokenName() + '|' + "QUALIFIED[TestWP1 (Test)]");
		}
	}

	@Test
	public void testRoundRobinQualifiedThreeOr()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(primaryContext, "TestWP3");
			construct(secondaryContext, "TestWP1");
			construct(secondaryContext, "TestWP2");
			construct(secondaryContext, "TestWP3");
			runRoundRobin(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2|TestWP3]");
		}
	}

	@Test
	public void testRoundRobinQualifiedThreeAnd()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "QUALIFIED[!TYPE=Type1,TYPE=Type2,TYPE=Type3]");
		}
	}

	@Test
	public void testRoundRobinQualifiedFourAndOr()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName()
					+ '|'
					+ "QUALIFIED[!TYPE=Type1,TYPE=Type2|!TYPE=Type3,TYPE=Type4]");
		}
	}

	@Test
	public void testRoundRobinQualifiedWithEqualType()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP1");
			construct(secondaryContext, "TestWP2");
			runRoundRobin(getSubTokenName()
					+ '|'
					+ "QUALIFIED[TestWP1|TestWP2|TYPE=OtherTestType|TYPE=TestType]");
		}
	}

	@Test
	public void testRoundRobinQualifiedTestEquals()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName() + '|' + "QUALIFIED[TYPE=TestType]");
		}
	}

	@Test
	public void testRoundRobinQualifiedTestEqualThree()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "QUALIFIED[TYPE=TestAltType.TestThirdType.TestType]");
		}
	}

	@Test
	public void testInvalidQualifiedInputAnyItem()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + "|QUALIFIED[ALL|TestWP1]"));
			assertNoSideEffects();
		}

	}

	@Test
	public void testInvalidQualifiedInputItemAny()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|ALL]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedInputAnyType()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[ALL|TYPE=TestType]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInvalidQualifiedInputTypeAny()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TYPE=TestType|ALL]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInputInvalidQualifiedAddsTypeNoSideEffect()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(secondaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP2");
			construct(primaryContext, "TestWP3");
			construct(secondaryContext, "TestWP3");
			assertTrue(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2]"));
			assertTrue(parseSecondary(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2]"));
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP3|TYPE=]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInputInvalidQualifiedAddsBasicNoSideEffect()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(secondaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP2");
			construct(primaryContext, "TestWP3");
			construct(secondaryContext, "TestWP3");
			construct(primaryContext, "TestWP4");
			construct(secondaryContext, "TestWP4");
			assertTrue(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2]"));
			assertTrue(parseSecondary(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2]"));
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP3||TestWP4]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testInputInvalidQualifiedAddsAllNoSideEffect()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			construct(primaryContext, "TestWP1");
			construct(secondaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP2");
			construct(primaryContext, "TestWP3");
			construct(secondaryContext, "TestWP3");
			assertTrue(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2]"));
			assertTrue(parseSecondary(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP1|TestWP2]"));
			assertFalse(parse(getSubTokenName() + '|'
					+ "QUALIFIED[TestWP3|ALL]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testRoundRobinTestQualifiedAll()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName() + "|QUALIFIED[ALL]");
		}
	}

	@Test
	public void testRoundRobinTestQualifiedAll2()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName() + "|ANY");
		}
	}

	@Test
	public void testRoundRobinTestQualifiedAll3()
			throws PersistenceLayerException
	{
		if (allowsQualifier())
		{
			runRoundRobin(getSubTokenName() + "|ANY[ALL]");
		}
	}

	@Test
	public void testUnparseNull() throws PersistenceLayerException
	{
		primaryProf.put(getObjectKey(), null);
		assertNull(getToken().unparse(primaryContext, primaryProf));
	}

	private ObjectKey<PersistentTransitionChoice<?>> getObjectKey()
	{
		return ObjectKey.CHOOSE_INFO;
	}

	@Test
	public void testUnparseIllegalAllItem() throws PersistenceLayerException
	{
		if (isAllLegal())
		{
			assertBadChoose("ALL|TestWP1");
		}
	}

	private void assertBadChoose(String value)
	{
		parseForUnparse(value, false);
		assertBadUnparse();
	}

	@Test
	public void testUnparseIllegalItemAll() throws PersistenceLayerException
	{
		if (isAllLegal())
		{
			assertBadChoose("ALL|TestWP1");
		}
	}

	@Test
	public void testUnparseIllegalAllType() throws PersistenceLayerException
	{
		if (isAllLegal())
		{
			assertBadChoose("ALL|TestWP1");
		}
	}

	@Test
	public void testUnparseIllegalTypeAll() throws PersistenceLayerException
	{
		if (isAllLegal())
		{
			assertBadChoose("ALL|TestWP1");
		}
	}

	@Test
	public void testUnparseLegal() throws PersistenceLayerException
	{
		assertGoodChoose("TestWP1|TestWP2");
	}

	private void assertGoodChoose(String value)
	{
		parseForUnparse(value, true);
		String[] unparse = getToken().unparse(primaryContext, primaryProf);
		assertNotNull(unparse);
		assertEquals(1, unparse.length);
		assertEquals(unparse[0], getSubToken().getTokenName() + "|" + value);
	}

	private void parseForUnparse(String value, boolean valid)
	{
		PrimitiveChoiceSet<TC> pcs = primaryContext.getChoiceSet(
				getManufacturer(), value);
		assertNotNull(pcs);
		assertEquals(valid, pcs.getGroupingState().isValid());
		ChoiceSet<TC> cs = new ChoiceSet<TC>(getSubToken().getTokenName(), pcs);
		cs.setTitle(getChoiceTitle());
		PersistentTransitionChoice<TC> tc = new PersistentTransitionChoice<TC>(
				cs, null);
		primaryProf.put(ObjectKey.CHOOSE_INFO, tc);
	}

	protected ReferenceManufacturer<TC> getManufacturer()
	{
		return primaryContext.ref.getManufacturer(getTargetClass());
	}

	protected abstract String getChoiceTitle();

	@Test
	public void testUnparseGenericsFail() throws PersistenceLayerException
	{
		ObjectKey objectKey = getObjectKey();
		primaryProf.put(objectKey, new Object());
		try
		{
			getToken().unparse(primaryContext, primaryProf);
			fail();
		}
		catch (ClassCastException e)
		{
			// Yep!
		}
	}

	@Test
	public void testValidQualifiedPCInputLotsOr()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "PC[TYPE=Bar|TYPE=Goo]|PC[TYPE=Foo|TYPE=Yea]");
		}
	}

	@Test
	public void testValidQualifiedPCInputLotsAnd()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "PC[TYPE=Bar,TYPE=Goo],PC[TYPE=Foo,TYPE=Yea]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCOne() throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			construct(primaryContext, "TestWP1");
			construct(secondaryContext, "TestWP1");
			runRoundRobin(getSubTokenName() + '|' + "PC[TestWP1]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCParen()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			construct(primaryContext, "TestWP1 (Test)");
			construct(secondaryContext, "TestWP1 (Test)");
			runRoundRobin(getSubTokenName() + '|' + "PC[TestWP1 (Test)]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCThreeOr()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			construct(primaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(primaryContext, "TestWP3");
			construct(secondaryContext, "TestWP1");
			construct(secondaryContext, "TestWP2");
			construct(secondaryContext, "TestWP3");
			runRoundRobin(getSubTokenName() + '|'
					+ "PC[TestWP1|TestWP2|TestWP3]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCThreeAnd()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "PC[!TYPE=Type1,TYPE=Type2,TYPE=Type3]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCFourAndOr()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "PC[!TYPE=Type1,TYPE=Type2|!TYPE=Type3,TYPE=Type4]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCNegatedFourAndOr()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "!PC[!TYPE=Type1,TYPE=Type2|!TYPE=Type3,TYPE=Type4]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCWithEqualType()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			construct(primaryContext, "TestWP1");
			construct(primaryContext, "TestWP2");
			construct(secondaryContext, "TestWP1");
			construct(secondaryContext, "TestWP2");
			runRoundRobin(getSubTokenName() + '|'
					+ "PC[TestWP1|TestWP2|TYPE=OtherTestType|TYPE=TestType]");
		}
	}

	@Test
	public void testRoundRobinQualifiedPCTestEquals()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + '|' + "PC[TYPE=TestType]");
		}
		else
		{
			assertFalse(parse(getSubTokenName() + '|' + "PC[TYPE=TestType]"));
			assertNoSideEffects();
		}
	}

	@Test
	public void testRoundRobinQualifiedPCTestEqualThree()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + '|'
					+ "PC[TYPE=TestAltType.TestThirdType.TestType]");
		}
	}

	@Test
	public void testRoundRobinTestQualifiedPCAll()
			throws PersistenceLayerException
	{
		if (allowsPCQualifier)
		{
			runRoundRobin(getSubTokenName() + "|PC[ALL]");
		}
	}

	public void stressOtherQualifier(String qual,
			Class<? extends CDOMObject> targetClass, boolean allowNegation)
			throws PersistenceLayerException
	{
		assertFalse(parse(getSubTokenName() + '|' + qual + "["));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + "|" + qual + "[|]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + "|" + qual + "[,]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TYPE=]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[!TYPE=]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TYPE=One.]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[!TYPE=One.]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[!TYPE=One..Two]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TYPE=.One]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TYPE=One..Two]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[!TYPE=.One]"));
		assertNoSideEffects();
		construct(primaryContext, "TestWP1");
		construct(secondaryContext, "TestWP1");
		construct(primaryContext, "TestWP2");
		construct(secondaryContext, "TestWP2");
		construct(primaryContext, "TestWP3");
		construct(secondaryContext, "TestWP3");
		construct(primaryContext, "TestWP4");
		construct(secondaryContext, "TestWP4");
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TestWP1|]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TestWP1,]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[|TestWP1]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[,TestWP1]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual
				+ "[TestWP2||TestWP1]]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual
				+ "[TYPE=Foo,,!TYPE=Bar]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual
				+ "[ALL|TYPE=TestType]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual
				+ "[TYPE=TestType|ALL]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TYPE=Foo]TestWP1"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual
				+ "[TestWP1]TYPE=Foo|TYPE=Bar"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TestWP1]TYPE=Foo"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual
				+ "[TYPE=Foo]TestWP1,TYPE=Bar"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + "|" + qual + "[ALL|TestWP1]"));
		assertNoSideEffects();
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TestWP1|ALL]"));
		assertNoSideEffects();
		assertTrue(parse(getSubTokenName() + '|' + qual + "[TestWP1|TestWP2]"));
		assertTrue(parseSecondary(getSubTokenName() + '|' + qual
				+ "[TestWP1|TestWP2]"));
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TestWP3|TYPE=]"));
		assertNoSideEffects();
		resetContext();
		assertTrue(parse(getSubTokenName() + '|' + qual + "." + qual));
		assertFalse(primaryContext.ref.validate(null));
		resetContext();
		assertTrue(parse(getSubTokenName() + '|' + qual + "[" + qual + "]"));
		assertFalse(primaryContext.ref.validate(null));
		resetContext();
		assertTrue(parse(getSubTokenName() + '|' + qual + "[String]"));
		assertFalse(primaryContext.ref.validate(null));
		resetContext();
		assertTrue(parse(getSubTokenName() + '|' + qual + "[TestWP1.TestWP2]"));
		assertFalse(primaryContext.ref.validate(null));
		resetContext();
		assertTrue(parse(getSubTokenName() + '|' + qual + "[TestWP1|TestWP2]"));
		assertTrue(parseSecondary(getSubTokenName() + '|' + qual
				+ "[TestWP1|TestWP2]"));
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TestWP3||TestWP4]"));
		assertNoSideEffects();
		resetContext();
		// Explicitly do NOT build TestWP0
		assertTrue(parse(getSubTokenName() + '|' + qual + "[TestWP0|TestWP1]"));
		assertFalse(primaryContext.ref.validate(null));
		resetContext();
		// this checks that the TYPE= doesn't consume the |
		assertTrue(parse(getSubTokenName() + '|' + qual
				+ "[TestWP1|TYPE=TestType|TestWP0]"));
		assertFalse(primaryContext.ref.validate(null));
		resetContext();
		// this checks that the TYPE. doesn't consume the |
		assertTrue(parse(getSubTokenName() + '|' + qual + "[TestWP1|"
				+ "TYPE.TestType.OtherTestType|TestWP2]"));
		assertFalse(primaryContext.ref.validate(null));
		resetContext();
		// Test with All
		assertTrue(parse(getSubTokenName() + '|' + qual + "[TestWP1|TestWP2]"));
		assertTrue(parseSecondary(getSubTokenName() + '|' + qual
				+ "[TestWP1|TestWP2]"));
		assertFalse(parse(getSubTokenName() + '|' + qual + "[TestWP3|ALL]"));
		assertNoSideEffects();
		if (!allowNegation)
		{
			resetContext();
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[TYPE=Bar|TYPE=Goo]|" + qual + "[TYPE=Foo|TYPE=Yea]"));
			assertNoSideEffects();
			resetContext();
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[TYPE=Bar,TYPE=Goo]," + qual + "[TYPE=Foo,TYPE=Yea]"));
			assertNoSideEffects();
			resetContext();
			construct(primaryContext, targetClass, "TestWP1");
			construct(secondaryContext, targetClass, "TestWP1");
			assertFalse(parse(getSubTokenName() + "|!" + qual + "[TestWP1]"));
			assertNoSideEffects();
			resetContext();
			construct(primaryContext, targetClass, "TestWP1 (Test)");
			construct(secondaryContext, targetClass, "TestWP1 (Test)");
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[TestWP1 (Test)]"));
			assertNoSideEffects();
			resetContext();
			construct(primaryContext, targetClass, "TestWP2");
			construct(primaryContext, targetClass, "TestWP3");
			construct(primaryContext, targetClass, "TestWP4");
			construct(secondaryContext, targetClass, "TestWP2");
			construct(secondaryContext, targetClass, "TestWP3");
			construct(secondaryContext, targetClass, "TestWP4");
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[TestWP2|TestWP3|TestWP4]"));
			assertNoSideEffects();
			resetContext();
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[!TYPE=Type1,TYPE=Type2,TYPE=Type3]"));
			assertNoSideEffects();
			resetContext();
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[!TYPE=Type1,TYPE=Type2|!TYPE=Type3,TYPE=Type4]"));
			assertNoSideEffects();
			resetContext();
			construct(primaryContext, targetClass, "TestWP5");
			construct(primaryContext, targetClass, "TestWP6");
			construct(secondaryContext, targetClass, "TestWP5");
			construct(secondaryContext, targetClass, "TestWP6");
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[TestWP5|TestWP6|TYPE=OtherTestType|TYPE=TestType]"));
			assertNoSideEffects();
			resetContext();
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[TYPE=TestType]"));
			assertNoSideEffects();
			resetContext();
			assertFalse(parse(getSubTokenName() + "|!" + qual
					+ "[TYPE=TestAltType.TestThirdType.TestType]"));
			assertNoSideEffects();
			resetContext();
			assertFalse(parse(getSubTokenName() + "|!" + qual + "[ALL]"));
			assertNoSideEffects();
			resetContext();
			//Invalid combination
			assertFalse(parse(getSubTokenName() + "|!" + qual + "[ALL]"));
			assertNoSideEffects();
		}
	}

	public void checkOtherQualifier(String qual,
			Class<? extends CDOMObject> targetClass, boolean allowNegation)
			throws PersistenceLayerException
	{
		runRoundRobin(getSubTokenName() + '|' + qual + "[TYPE=Bar|TYPE=Goo]|"
				+ qual + "[TYPE=Foo|TYPE=Yea]");
		resetContext();
		runRoundRobin(getSubTokenName() + '|' + qual + "[TYPE=Bar,TYPE=Goo],"
				+ qual + "[TYPE=Foo,TYPE=Yea]");
		resetContext();
		construct(primaryContext, targetClass, "TestWP1");
		construct(secondaryContext, targetClass, "TestWP1");
		runRoundRobin(getSubTokenName() + '|' + qual + "[TestWP1]");
		resetContext();
		construct(primaryContext, targetClass, "TestWP1 (Test)");
		construct(secondaryContext, targetClass, "TestWP1 (Test)");
		runRoundRobin(getSubTokenName() + '|' + qual + "[TestWP1 (Test)]");
		resetContext();
		construct(primaryContext, targetClass, "TestWP2");
		construct(primaryContext, targetClass, "TestWP3");
		construct(primaryContext, targetClass, "TestWP4");
		construct(secondaryContext, targetClass, "TestWP2");
		construct(secondaryContext, targetClass, "TestWP3");
		construct(secondaryContext, targetClass, "TestWP4");
		runRoundRobin(getSubTokenName() + '|' + qual
				+ "[TestWP2|TestWP3|TestWP4]");
		resetContext();
		runRoundRobin(getSubTokenName() + '|' + qual
				+ "[!TYPE=Type1,TYPE=Type2,TYPE=Type3]");
		resetContext();
		runRoundRobin(getSubTokenName() + '|' + qual
				+ "[!TYPE=Type1,TYPE=Type2|!TYPE=Type3,TYPE=Type4]");
		resetContext();
		construct(primaryContext, targetClass, "TestWP5");
		construct(primaryContext, targetClass, "TestWP6");
		construct(secondaryContext, targetClass, "TestWP5");
		construct(secondaryContext, targetClass, "TestWP6");
		runRoundRobin(getSubTokenName() + '|' + qual
				+ "[TestWP5|TestWP6|TYPE=OtherTestType|TYPE=TestType]");
		resetContext();
		runRoundRobin(getSubTokenName() + '|' + qual + "[TYPE=TestType]");
		resetContext();
		runRoundRobin(getSubTokenName() + '|' + qual
				+ "[TYPE=TestAltType.TestThirdType.TestType]");
		resetContext();
		runRoundRobin(getSubTokenName() + '|' + qual + "[ALL]");
		if (allowNegation)
		{
			resetContext();
			runRoundRobin(getSubTokenName() + "|!" + qual
					+ "[TYPE=Bar|TYPE=Goo]|" + qual + "[TYPE=Foo|TYPE=Yea]");
			resetContext();
			runRoundRobin(getSubTokenName() + "|!" + qual
					+ "[TYPE=Bar,TYPE=Goo]," + qual + "[TYPE=Foo,TYPE=Yea]");
			resetContext();
			construct(primaryContext, targetClass, "TestWP1");
			construct(secondaryContext, targetClass, "TestWP1");
			runRoundRobin(getSubTokenName() + "|!" + qual + "[TestWP1]");
			resetContext();
			construct(primaryContext, targetClass, "TestWP1 (Test)");
			construct(secondaryContext, targetClass, "TestWP1 (Test)");
			runRoundRobin(getSubTokenName() + "|!" + qual + "[TestWP1 (Test)]");
			resetContext();
			construct(primaryContext, targetClass, "TestWP2");
			construct(primaryContext, targetClass, "TestWP3");
			construct(primaryContext, targetClass, "TestWP4");
			construct(secondaryContext, targetClass, "TestWP2");
			construct(secondaryContext, targetClass, "TestWP3");
			construct(secondaryContext, targetClass, "TestWP4");
			runRoundRobin(getSubTokenName() + "|!" + qual
					+ "[TestWP2|TestWP3|TestWP4]");
			resetContext();
			runRoundRobin(getSubTokenName() + "|!" + qual
					+ "[!TYPE=Type1,TYPE=Type2,TYPE=Type3]");
			resetContext();
			runRoundRobin(getSubTokenName() + "|!" + qual
					+ "[!TYPE=Type1,TYPE=Type2|!TYPE=Type3,TYPE=Type4]");
			resetContext();
			construct(primaryContext, targetClass, "TestWP5");
			construct(primaryContext, targetClass, "TestWP6");
			construct(secondaryContext, targetClass, "TestWP5");
			construct(secondaryContext, targetClass, "TestWP6");
			runRoundRobin(getSubTokenName() + "|!" + qual
					+ "[TestWP5|TestWP6|TYPE=OtherTestType|TYPE=TestType]");
			resetContext();
			runRoundRobin(getSubTokenName() + "|!" + qual + "[TYPE=TestType]");
			resetContext();
			runRoundRobin(getSubTokenName() + "|!" + qual
					+ "[TYPE=TestAltType.TestThirdType.TestType]");
		}
	}
}