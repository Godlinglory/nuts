/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.string;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class StringNNoteEditorTypeComponent extends JPanel implements NNoteEditorTypeComponent {

    private JTextField text = new JTextField();
    private VNNote currentNode;

    public StringNNoteEditorTypeComponent() {
        GridBagLayoutSupport.of("[^$-==item]")
                .bind("item", text)
                .apply(this);
        text.getDocument().addDocumentListener(new AnyDocumentListener() {
            @Override
            public void anyChange(DocumentEvent e) {
                if (currentNode != null) {
                    currentNode.setContent(text.getText());
                }
            }
        });
//        setBorder(BorderFactory.createLineBorder(Color.red));
    }

    @Override
    public JComponent component() {
        return this;
    }

    @Override
    public void uninstall() {
    }

    @Override
    public void setNode(VNNote node, NNoteGuiApp sapp) {
        this.currentNode = node;
        if (node == null) {
            text.setText("");
        } else {
            text.setText(node.getContent());
        }

    }

}
