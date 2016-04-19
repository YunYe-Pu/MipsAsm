package mipsAsm.util;

import javafx.beans.binding.BooleanExpression;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;

public class MenuHelper
{
	public static MenuItem item(String title, EventHandler<ActionEvent> onAction,
			BooleanExpression disableBinding, String character, Modifier... modifier)
	{
		MenuItem ret = new MenuItem(title);
		ret.setOnAction(onAction);
		ret.setAccelerator(new KeyCharacterCombination(character, modifier));
		ret.disableProperty().bind(disableBinding);
		return ret;
	}

	public static MenuItem item(String title, EventHandler<ActionEvent> onAction,
			BooleanExpression disableBinding, KeyCode keyCode, Modifier... modifier)
	{
		MenuItem ret = new MenuItem(title);
		ret.setOnAction(onAction);
		ret.setAccelerator(new KeyCodeCombination(keyCode, modifier));
		ret.disableProperty().bind(disableBinding);
		return ret;
	}
	
	public static MenuItem item(String title, EventHandler<ActionEvent> onAction, BooleanExpression disableBinding)
	{
		MenuItem ret = new MenuItem(title);
		ret.setOnAction(onAction);
		ret.disableProperty().bind(disableBinding);
		return ret;
	}
	
	public static Menu menu(String title, MenuItem[] menuItems, String character)
	{
		Menu ret = new Menu(title);
		ret.setAccelerator(new KeyCharacterCombination(character, KeyCombination.ALT_DOWN));
		ret.getItems().addAll(menuItems);
		return ret;
	}
	

}
