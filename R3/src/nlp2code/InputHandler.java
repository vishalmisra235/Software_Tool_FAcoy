package nlp2code;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

 class MyTitleAreaDialog extends TitleAreaDialog {

    private Text txtFirstName;
    private Text lastNameText;

    private String firstName;
    private String lastName;

    public MyTitleAreaDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("This is my first custom dialog");
        setMessage("This is a TitleAreaDialog", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        createFirstName(container);
        createLastName(container);

        return area;
    }

    private void createFirstName(Composite container) {
        Label lbtFirstName = new Label(container, SWT.NONE);
        lbtFirstName.setText("First Name");

        GridData dataFirstName = new GridData();
        dataFirstName.grabExcessHorizontalSpace = true;
        dataFirstName.horizontalAlignment = GridData.FILL;

        txtFirstName = new Text(container, SWT.BORDER);
        txtFirstName.setLayoutData(dataFirstName);
    }

    private void createLastName(Composite container) {
        Label lbtLastName = new Label(container, SWT.NONE);
        lbtLastName.setText("Last Name");

        GridData dataLastName = new GridData();
        dataLastName.grabExcessHorizontalSpace = true;
        dataLastName.horizontalAlignment = GridData.FILL;
        lastNameText = new Text(container, SWT.BORDER);
        lastNameText.setLayoutData(dataLastName);
    }



    @Override
    protected boolean isResizable() {
        return true;
    }

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private void saveInput() {
        firstName = txtFirstName.getText();
        lastName = lastNameText.getText();

    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
/**
 * class InputHandler
 *  Implements the required functionality to search for a query via the search button in the eclipse toolbar.
 */
public class InputHandler extends AbstractHandler {
	// Holds history of previous code snippets (from previous queries) to enable undo functionality.
	static Vector<String> previous_search = new Vector<String>();
	// Holds the previous query (equivilent to previous_search[last]).
	static String previous_query = "";
	// Offset of the previous query (to re-insert when using undo).
	static int previous_offset = 0;
	// Length of the previous query (to re-insert when using undo).
	static int previous_length = 0;
	// Holds previous queries.
	static Vector<String> previous_queries = new Vector<String>();
	// Listens for when cycling finishes and prompts for feedback afterwards.
	static CycleDocListener doclistener = new CycleDocListener();
	// Create a listener to handle searches via the editor in ?{query}? format.
	static QueryDocListener qdl = new QueryDocListener();
	// A vector containing all documents that have an active query document listener.
	static Vector<IDocument> documents = new Vector<IDocument>();
	ArrayList<String> results = new ArrayList<String>();
	ArrayList<Double> new_results = new ArrayList<Double>();
	TreeMap<String, Double> mp = new TreeMap<String, Double>();
	String original_text= "";
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
				try {               
				    IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				    if ( part instanceof ITextEditor ) {
				        final ITextEditor editor = (ITextEditor)part;
				        IDocumentProvider prov = editor.getDocumentProvider();
				        
				   
						IDocument doc = prov.getDocument( editor.getEditorInput() );
				        ISelection sel = editor.getSelectionProvider().getSelection();
				        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
				        int offset = doc.getLineOffset(doc.getNumberOfLines()-1);
				        
				        if ( sel instanceof TextSelection ) {
				            final TextSelection textSel = (TextSelection)sel;
				            String txt=textSel.getText();
				            List<String> list = new ArrayList<String>(Arrays.asList(txt.split("\n")));
				            for(String str : list)
				            {
				                System.out.println(str);
				            }
				           MessageDialog.openInformation(window.getShell(),"Selected text","Your selected text is\n\n" + txt);
				          //  ElementListSelectionDialog listDialog = new ElementListSelectionDialog(window.getShell(),new LabelProvider());	    
				    	   // listDialog.setTitle("This is Element List Title");
				    	   // listDialog.setMessage("This is Element List Message");
				    	   // listDialog.setEmptyListMessage(txt);
				    	   // for(Object str : list.toArray())
				           // {
				           //     System.out.println(str);
				           // }
				    	   // listDialog.setElements(list.toArray());
				    	   // listDialog.open();
				            scrap(txt);
				            original_text = txt;
				            
				            System.out.println("hi");
				        }
				        
				        Similarity s = new Similarity();
				        
				        for(String x:results)
				        {
				        	double score = s.Cosine_Similarity_Score(x, original_text);
				        	new_results.add(score);
				        	mp.put(x, score);
				        }
				        
				        List<Map.Entry<String, Double> > list = 
				                new LinkedList<Map.Entry<String, Double> >(mp.entrySet()); 
				   
				         // Sort the list 
				         Collections.sort(list, new Comparator<Map.Entry<String, Double> >() { 
				             public int compare(Map.Entry<String, Double> o1,  
				                                Map.Entry<String, Double> o2) 
				             { 
				                 return (o1.getValue()).compareTo(o2.getValue())*(-1); 
				             } 
				         }); 
				           
				         // put data from sorted list to hashmap  
				         HashMap<String, Double> mpnew = new LinkedHashMap<String, Double>(); 
				         for (Entry<String, Double> aa : list) { 
				             mpnew.put(aa.getKey(), aa.getValue()); 
				         } 
				        
				        for(Entry<String, Double> e : mpnew.entrySet())
				        {
				        	String key = e.getKey();
				        	Double value = e.getValue();
				        	//List<String> list2 = new ArrayList<String>(Arrays.asList(key.split("\n")));
				        	MessageDialog dialog = new MessageDialog(window.getShell(),"Codes",null,"Similarity Score: "+Double.toString(value)+"\n"+key,MessageDialog.INFORMATION, new String[] {"Next Code","Close","Copy"},0);
				        	// ElementListSelectionDialog listDialog1 = new ElementListSelectionDialog(window.getShell(),new LabelProvider());	    
					    	  //  listDialog1.setTitle("This is Element List Title");
					    	  //  listDialog1.setMessage(Double.toString(value));
					    	   // listDialog.setEmptyListMessage(txt);
					    	  //  listDialog1.setElements(list2.toArray());
					    	   // listDialog1.open();
				        int a =dialog.open();
				        	//int a =listDialog1.open();
				        	//System.out.println(a);
				        	
				        	if(a==0)
				        	{
				        		continue;
				        	}
				        	else if(a==2)
				        	{
				        		doc.replace(offset, 0, key+"\n");
				        		break;
				        	}
				        	else
				        	{
				        		break;
				        	}
				        }
				        
				    }
				} 
				
				catch( Exception ex ){
				    ex.printStackTrace();
				}
				return null;
			}
			private void scrap(String txt) {
				Document doc = null;
				try {
					 doc = Jsoup.connect("http://code-search.uni.lu/facoy").data("q",txt).timeout(10 * 1000).get();
					 for (org.jsoup.nodes.Element item : doc.select("div.snippet_item table.highlighttable tr")) {
						String data_item=item.select(".code").text();
						results.add(data_item);
						//System.out.println(data_item);
						//System.out.println("1111");
					}
					 
				} catch (IOException e) {
					System.out.println("Submission failed");
					e.printStackTrace();
				}
				System.out.println("hi2");
			//}
			
}
}