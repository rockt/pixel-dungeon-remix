package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.HeroSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class BerserkArmor extends ClassArmor {
	
	private static int LEAP_TIME	= 1;
	private static int SHOCK_TIME	= 3;
	
	private static final String AC_SPECIAL = Game.getVar(R.string.WarriorArmor_ACSpecial); 
	
	private static final String TXT_NOT_WARRIOR	= Game.getVar(R.string.WarriorArmor_NotWarrior);
	
	{
		image = 6;
	}
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
		GameScene.selectCell( leaper );
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.WARRIOR && hero.subClass == HeroSubClass.BERSERKER) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_WARRIOR );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.WarriorArmor_Desc);
	}
	
	protected static CellSelector.Listener leaper = new  CellSelector.Listener() {
		
		@Override
		public void onSelect( Integer target ) {
			if (target != null && target != curUser.pos) {
				
				int cell = Ballistica.cast( curUser.pos, target, false, true );
				if (Actor.findChar( cell ) != null && cell != curUser.pos) {
					cell = Ballistica.trace[Ballistica.distance - 2];
				}
				
				curUser.hp(curUser.hp() - (curUser.hp() / 3));
				
				curUser.checkIfFurious();
				
				Invisibility.dispel(curUser);
				
				final int dest = cell;
				curUser.busy();
				((HeroSprite)curUser.getSprite()).jump( curUser.pos, cell, new Callback() {
					@Override
					public void call() {
						curUser.move( dest );
						Dungeon.level.press( dest, curUser );
						Dungeon.observe();
						
						for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
							Char mob = Actor.findChar( curUser.pos + Level.NEIGHBOURS8[i] );
							if (mob != null && mob != curUser) {
								Buff.prolong( mob, Paralysis.class, SHOCK_TIME );
							}
						}
						
						CellEmitter.center( dest ).burst( Speck.factory( Speck.DUST ), 10 );
						Camera.main.shake( 2, 0.5f );
						
						curUser.spendAndNext( LEAP_TIME );
					}
				} );
			}
		}
		
		@Override
		public String prompt() {
			return Game.getVar(R.string.WarriorArmor_Prompt);
		}
	};
}