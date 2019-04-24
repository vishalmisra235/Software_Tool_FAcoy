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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
class BrowserDialog extends Dialog {

    private String browserString;

    protected BrowserDialog(Shell parentShell, String browserString) {
        super(parentShell);
        this.browserString = browserString;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = 600;
        data.heightHint = 600;
        composite.setLayoutData(data);


        Browser browser = new Browser(composite, SWT.NONE);
        browser.setText(browserString);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, Dialog.OK, "OK", true);
        createButton(parent, Dialog.CANCEL, "Cancel", false);
    }
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Your Selected Code");
    }

    @Override
    public void okPressed() {
        close();
    }
}
class BrowserDialog1 extends Dialog {

    private String browserString;

    protected BrowserDialog1(Shell parentShell, String browserString) {
        super(parentShell);
        this.browserString = browserString;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = 600;
        data.heightHint = 600;
        composite.setLayoutData(data);


        Browser browser = new Browser(composite, SWT.NONE);
        browser.setText(browserString);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, 0, "Next Code", true);
        createButton(parent, 1, "Copy Code", false);
        createButton(parent, 2, "Close", false);
       
    }
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Retrieved Code");
    }

    @Override
    public void okPressed() {
        close();
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
				        int y=0;
				        if ( sel instanceof TextSelection ) {
				            final TextSelection textSel = (TextSelection)sel;
				            String txt=textSel.getText();
				            List<String> list = new ArrayList<String>(Arrays.asList(txt.split("\n")));
				            for(String str : list)
				            {
				               // System.out.println(str);
				            }
				          //  MyTitleAreaDialog x=new MyTitleAreaDialog(window.getShell());
				           // x.create();
				            String text="<h3>Your selected text is:</h3>\n\n"+txt;
//				            final Display display = new Display();
//				            Shell shell = new Shell(display);

				           // Color gray = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

				         //   String hex = String.format("#%02x%02x%02x", gray.getRed(), gray.getGreen(), gray.getBlue());

				            y=new BrowserDialog(window.getShell(), "<body>"+text+"</body>").open();
				            System.out.println(y);
				            //MessageDialog.openInformation(window.getShell(),"Selected text",text);
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
				            if(y==0) {
				            	scrap(txt);
				            }
				            original_text = txt;
				            
				            System.out.println("hi");
				        }
				        if(y==0) {
				        Similarity s = new Similarity();
				        
				        for(String x:results)
				        {
				        	double score = s.Similarity_Score(x, original_text);
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
				        	 int a=new BrowserDialog1(window.getShell(), "<body>"+"<h3>Similarity Score: "+Double.toString(value)+"</h3>\n"+key+"</body>").open();
				        	//MessageDialog dialog = new MessageDialog(window.getShell(),"Codes",null,"Similarity Score: "+Double.toString(value)+"\n"+key,MessageDialog.INFORMATION, new String[] {"Next Code","Close","Copy"},0);
				        	// ElementListSelectionDialog listDialog1 = new ElementListSelectionDialog(window.getShell(),new LabelProvider());	    
					    	  //  listDialog1.setTitle("This is Element List Title");
					    	  //  listDialog1.setMessage(Double.toString(value));
					    	   // listDialog.setEmptyListMessage(txt);
					    	  //  listDialog1.setElements(list2.toArray());
					    	   // listDialog1.open();
				       // int a =dialog.open();
				        	//int a =listDialog1.open();
				        	//System.out.println(a);
				        	
				        	if(a==0)
				        	{
				        		continue;
				        	}
				        	else if(a==1)
				        	{
				        		doc.replace(offset, 0, key+"\n");
				        		break;
				        	}
				        	else
				        	{
				        		break;
				        	}
				        }
				        
				    }}
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
