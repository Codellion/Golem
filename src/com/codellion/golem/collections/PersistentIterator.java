package com.codellion.golem.collections;

import java.util.Iterator;

import com.codellion.golem.wrappers.StoneGolem;

public class PersistentIterator implements Iterator<StoneGolem> {

	Iterator<Node> superIterator;
	
	public PersistentIterator(Iterator<Node> superIterator)
	{
		this.superIterator = superIterator;
	}
	
	
	public boolean hasNext() {
		return superIterator.hasNext();
	}

	public StoneGolem next() {
		Node node = superIterator.next();
		
		StoneGolem stoneGolem = node.valor;
		
		if(!stoneGolem.getFetched())
			stoneGolem.fetchFullValue();
		
		return stoneGolem;
	}

	public void remove() {
		superIterator.remove();
	}

}
