// Copyright (c) 2007 Martin Matusiak <numerodix@gmail.com>
// Licensed under the GNU Public License, version 3.

package mud.common;

public interface GameInfo {
	
	public enum RoomInfo { NAME, EXITS, HUMANS, MONSTERS, WEAPONS, ARMOR, FOOD }
	public enum CharacterInventory { WEAPONS, ARMOR, FOOD }
	public enum CharacterType { HUMAN, MONSTER }
	
}
