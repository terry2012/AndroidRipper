package it.unina.android.ripper.planner;

import it.unina.android.ripper.constants.InteractionType;
import it.unina.android.ripper.constants.SimpleType;
import it.unina.android.ripper.model.ActivityDescription;
import it.unina.android.ripper.model.Input;
import it.unina.android.ripper.model.Task;
import it.unina.android.ripper.model.WidgetDescription;
import it.unina.android.ripper.planner.task.TaskList;
import it.unina.android.ripper.planner.widget_events.ListViewEventPlanner;
import it.unina.android.ripper.planner.widget_events.RadioGroupEventPlanner;
import it.unina.android.ripper.planner.widget_events.SeekBarEventPlanner;
import it.unina.android.ripper.planner.widget_events.SpinnerEventPlanner;
import it.unina.android.ripper.planner.widget_events.TextViewEventPlanner;
import it.unina.android.ripper.planner.widget_events.WidgetEventPlanner;
import it.unina.android.ripper.planner.widget_inputs.ClickableWidgetInputPlanner;
import it.unina.android.ripper.planner.widget_inputs.EditTextInputPlanner;
import it.unina.android.ripper.planner.widget_inputs.SpinnerInputPlanner;
import it.unina.android.ripper.planner.widget_inputs.WidgetInputPlanner;
import it.unina.android.ripper.planner.widget_inputs.values_generator.RandomNumericValuesGenerator;

import java.util.ArrayList;

/*
 * NOTA: max_event_len -> if task.size() > MAX don't add :-)
 */
public class HandlerBasedPlanner extends Planner
{
	public static int MAX_INTERACTIONS_FOR_LIST = 9999;
	public static int MAX_INTERACTIONS_FOR_PREFERENCES_LIST = 9999;
	public static int MAX_INTERACTIONS_FOR_SINGLE_CHOICE_LIST = 9999;
	public static int MAX_INTERACTIONS_FOR_MULTI_CHOICE_LIST = 9999;
	public static int MAX_INTERACTIONS_FOR_SPINNER = 9999;
	
	public static boolean CAN_GO_BACK = true;
	public static boolean CAN_CHANGE_ORIENTATION = true;
	public static boolean CAN_OPEN_MENU = true;
	public static boolean CAN_SCROLL_DOWN = false;
	public static boolean CAN_GENERATE_KEY_PRESS_EVENTS = false;
	public static boolean CAN_GENERATE_LONG_KEY_PRESS_EVENTS = false;
	public static boolean CAN_SWAP_TAB = true;
	
	public static String[] inputWidgetList = {
		SimpleType.EDIT_TEXT
		//,SimpleType.SPINNER
		//,SimpleType.CHECKBOX
		//,SimpleType.RADIO
		//,SimpleType.TOGGLE
		//,SimpleType.SEEK_BAR
		//,SimpleType.RATING_BAR
		//,SimpleType.FOCUSABLE_EDIT_TEXT
	};
	
	protected boolean isInputWidget(WidgetDescription widget)
	{
		for (String s : inputWidgetList)
			if(s.equals(widget.getSimpleType()))
				return true;

		return false;
	}
	
	public HandlerBasedPlanner()
	{
		super();
	}
	
	@Override
	public TaskList plan(Task currentTask, ActivityDescription activity, String... options)
	{
		TaskList taskList = new TaskList();
		
		//add activity interactions
		taskList.addAll(this.planForActivity(currentTask, activity, options));

		//generate inputs
		ArrayList<Input> inputs = new ArrayList<Input>();
		for (WidgetDescription wd: activity.getWidgets())
		{
			Input input = getInputForWidget(wd);
			if (input != null)
				inputs.add(input);
		}
		
		//TODO: set extra inputs
		
		//widgets interactions
		for (WidgetDescription wd: activity.getWidgets())
		{
			if (wd.isEnabled() && wd.isVisible())
			{
				TaskList tList = this.planForWidget(currentTask, wd, inputs, options);
				if (tList != null)
					taskList.addAll(tList);
			}
		}	
		
		//TODO: add extra events
		
		return taskList;
	}
	
	protected TaskList planForActivity(Task currentTask, ActivityDescription activity, String... options)
	{
		TaskList taskList = new TaskList();
		
		if (CAN_GO_BACK)
			taskList.addNewTaskForActivity(currentTask, InteractionType.BACK);
		
		if (CAN_CHANGE_ORIENTATION)
			taskList.addNewTaskForActivity(currentTask, InteractionType.CHANGE_ORIENTATION);
		
		if (CAN_OPEN_MENU && activity.hasMenu())
			taskList.addNewTaskForActivity(currentTask, InteractionType.OPEN_MENU);

		if (CAN_SCROLL_DOWN)
			taskList.addNewTaskForActivity(currentTask, InteractionType.SCROLL_DOWN);
		
		if (CAN_GENERATE_KEY_PRESS_EVENTS && activity.handlesKeyPress())
		{
			//TODO
		}
		
		if (CAN_GENERATE_LONG_KEY_PRESS_EVENTS && activity.handlesLongKeyPress())
		{
			//TODO
		}
		
		if (CAN_SWAP_TAB && activity.isTabActivity())
			for(int i = 1; i <= activity.getTabsCount(); i++)
				taskList.addNewTaskForActivity(currentTask, InteractionType.SWAP_TAB, Integer.toString(i));
		
		//TODO: sensors / gps / intents
		//...
		//...
		
		return taskList;
	}
	
	protected TaskList planForWidget(Task currentTask, WidgetDescription widgetDescription, ArrayList<Input> inputs, String... options)
	{
		//excludes widgets used as input
		if (isInputWidget(widgetDescription) == false)
		{
			WidgetEventPlanner widgetEventPlanner;
			
			//expandmenu == list
			//numberpicker -> click?
			//auto_complete_text
			//search_bar
			
			if (widgetDescription.getSimpleType().equals(SimpleType.LIST_VIEW))
			{
				widgetEventPlanner = new ListViewEventPlanner(widgetDescription, MAX_INTERACTIONS_FOR_LIST);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.PREFERENCE_LIST))
			{
				widgetEventPlanner = new ListViewEventPlanner(widgetDescription, MAX_INTERACTIONS_FOR_PREFERENCES_LIST);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.SINGLE_CHOICE_LIST))
			{
				widgetEventPlanner =  new ListViewEventPlanner(widgetDescription, MAX_INTERACTIONS_FOR_SINGLE_CHOICE_LIST);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.MULTI_CHOICE_LIST))
			{
				widgetEventPlanner =  new ListViewEventPlanner(widgetDescription, MAX_INTERACTIONS_FOR_MULTI_CHOICE_LIST);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.SPINNER))
			{
				widgetEventPlanner =  new SpinnerEventPlanner(widgetDescription, MAX_INTERACTIONS_FOR_SPINNER);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.RADIO_GROUP))
			{
				widgetEventPlanner =  new RadioGroupEventPlanner(widgetDescription, MAX_INTERACTIONS_FOR_SPINNER);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.TEXT_VIEW))
			{
				widgetEventPlanner =  new TextViewEventPlanner(widgetDescription);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.SEEK_BAR) || widgetDescription.getSimpleType().equals(SimpleType.RATING_BAR))
			{
				RandomNumericValuesGenerator randomValuesGenerator = new RandomNumericValuesGenerator(0,99);
				widgetEventPlanner = new SeekBarEventPlanner(widgetDescription, randomValuesGenerator);
			}
			else
			{
				widgetEventPlanner =  new WidgetEventPlanner(widgetDescription);
			}
			
			return widgetEventPlanner.planForWidget(currentTask, inputs, options);
		}
		else
		{
			return null; //widget is an input
		}
	}
	
	protected Input getInputForWidget(WidgetDescription widgetDescription)
	{
		if (isInputWidget(widgetDescription))
		{
			WidgetInputPlanner widgetInputPlanner = null;
			
			if (widgetDescription.getSimpleType().equals(SimpleType.EDIT_TEXT))
			{
				RandomNumericValuesGenerator randomValuesGenerator = new RandomNumericValuesGenerator(0,99);
				widgetInputPlanner = new EditTextInputPlanner(widgetDescription, randomValuesGenerator);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.SPINNER))
			{
				RandomNumericValuesGenerator randomValuesGenerator = new RandomNumericValuesGenerator(0,widgetDescription.getCount());
				widgetInputPlanner = new SpinnerInputPlanner(widgetDescription, randomValuesGenerator);
			}
			else if (widgetDescription.getSimpleType().equals(SimpleType.RADIO_GROUP))
			{
				RandomNumericValuesGenerator randomValuesGenerator = new RandomNumericValuesGenerator(0,widgetDescription.getCount());
				widgetInputPlanner = new SpinnerInputPlanner(widgetDescription, randomValuesGenerator);
			}
			else
			{
				widgetInputPlanner = new ClickableWidgetInputPlanner(widgetDescription);
			}
			
			return widgetInputPlanner.getInputForWidget();
		}
		else
		{
			return null; //widget is not an input
		}
	}
}
