/*
 * Copyright © 2019-2025 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gui.editor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * IP custom field.
 * @author KenM
 * @version 1.0.0
 */
public class JIPTextField extends JPanel implements FocusListener {
	private static final long serialVersionUID = 1L;
	private JTextField[] _textFields;
	private List<FocusListener> _focusListeners;
	
	public JIPTextField(String textIp) {
		super.addFocusListener(this);
		
		initIPTextField(textIp);
		
		for (JTextField _textField : _textFields) {
			_textField.addFocusListener(this);
		}
	}
	
	public JIPTextField() {
		this("...");
	}
	
	public JIPTextField(Inet4Address value) {
		this(value.getHostAddress());
	}
	
	private void initIPTextField(String textIp) {
		final ActionListener nextfocusaction = evt -> ((Component) evt.getSource()).transferFocus();
		
		setLayout(new GridBagLayout());
		_textFields = new JTextField[4];
		
		var cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.PAGE_START;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.insets = new Insets(1, 1, 1, 1);
		cons.gridx = 0;
		cons.gridy = 0;
		
		MaxLengthDocument previous = null;
		var parts = textIp.split("\\.");
		for (var i = 0; i < 4; i++) {
			var str = parts[i];
			if (i > 0) {
				var dot = new JLabel(".");
				cons.weightx = 0;
				add(dot, cons);
				cons.gridx++;
			}
			var maxDoc = new MaxLengthDocument(3);
			_textFields[i] = new JTextField(maxDoc, str, 3);
			if (previous != null) {
				previous.setNext(_textFields[i]);
			}
			previous = maxDoc;
			add(_textFields[i], cons);
			_textFields[i].addActionListener(nextfocusaction);
			cons.gridx++;
		}
	}
	
	@Override
	public void addFocusListener(FocusListener fl) {
		if (_focusListeners == null) {
			_focusListeners = new LinkedList<>();
		}
		
		if ((fl != null) && !_focusListeners.contains(fl)) {
			_focusListeners.add(fl);
		}
	}
	
	@Override
	public void removeFocusListener(FocusListener fl) {
		if (_focusListeners != null) {
			_focusListeners.remove(fl);
		}
	}
	
	public String getText() {
		var str = new StringBuilder();
		for (var i = 0; i < 4; i++) {
			if (_textFields[i].getText().length() == 0) {
				str.append('0');
			} else {
				str.append(_textFields[i].getText());
			}
			
			if (i < 3) {
				str.append('.');
			}
		}
		return str.toString();
	}
	
	public void setText(String str) {
		try {
			var ip = InetAddress.getByName(str);
			byte b[] = ip.getAddress();
			for (var i = 0; i < 4; i++) {
				// byte always have a sign in Java, IP addresses aren't
				if (b[i] >= 0) {
					_textFields[i].setText(Byte.toString(b[i]));
				} else {
					_textFields[i].setText(Integer.toString(b[i] + 256));
				}
			}
			return;
		} catch (UnknownHostException ex) {
		} catch (NullPointerException npe) {
		}
		for (var i = 0; i < 4; i++) {
			_textFields[i].setText("");
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		for (JTextField _textField : _textFields) {
			if (_textField != null) {
				_textField.setEnabled(enabled);
			}
		}
	}
	
	public boolean isEmpty() {
		for (var i = 0; i < 4; i++) {
			if (!_textFields[i].getText().isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isCorrect() {
		for (var i = 0; i < 4; i++) {
			if (_textFields[i].getText().length() == 0) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void focusGained(FocusEvent event) {
		if (_focusListeners != null) {
			for (FocusListener fl : _focusListeners) {
				fl.focusGained(event);
			}
		}
	}
	
	@Override
	public void focusLost(FocusEvent event) {
		if (isCorrect() || isEmpty()) {
			if (_focusListeners != null) {
				for (FocusListener fl : _focusListeners) {
					fl.focusLost(event);
				}
			}
		}
	}
	
	public class MaxLengthDocument extends PlainDocument {
		
		private static final long serialVersionUID = 1L;
		
		private final int _max;
		private JTextField _next;
		
		public MaxLengthDocument(int maxLength) {
			this(maxLength, null);
		}
		
		public MaxLengthDocument(int maxLength, JTextField next) {
			_max = maxLength;
			setNext(next);
		}
		
		@Override
		public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
			if ((getLength() + str.length()) > _max) {
				if (getNext() != null) {
					if (getNext().getText().length() > 0) {
						getNext().select(0, getNext().getText().length());
					} else {
						getNext().getDocument().insertString(0, str, a);
					}
					getNext().requestFocusInWindow();
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			} else {
				super.insertString(offset, str, a);
			}
		}
		
		public void setNext(JTextField next) {
			_next = next;
		}
		
		public JTextField getNext() {
			return _next;
		}
	}
}
