/*******************************************************************************
 * KOMORAN 3.0 - Korean Morphology Analyzer
 *
 * Copyright 2015 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package kr.co.shineware.nlp.komoran.modeler.model;

import kr.co.shineware.nlp.komoran.interfaces.FileAccessible;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Transition implements FileAccessible{

	private double[][] scoreMatrix;

	public Transition(){
    }

	public Transition(int size) {
		scoreMatrix = new double[size][size];
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				scoreMatrix[i][j] = Double.NEGATIVE_INFINITY;
			}
		}
	}

	public void put(int prevId, int curId, double transitionScore) {
		scoreMatrix[prevId][curId] = transitionScore;
	}
	public Double get(int prevId, int curId){
		double val = scoreMatrix[prevId][curId];
		if(val == Double.NEGATIVE_INFINITY){
			return null;
		}
		return val;
	}

	public double getScore(int prevId, int curId){
		return scoreMatrix[prevId][curId];
	}

	public boolean hasTransition(int prevId, int curId){
		return scoreMatrix[prevId][curId] != Double.NEGATIVE_INFINITY;
	}

	@Override
	public void save(String filename) {
		try (ObjectOutputStream dos = new ObjectOutputStream(
				new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))))) {
			dos.writeObject(scoreMatrix);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void load(String filename) {
		try (ObjectInputStream dis = new ObjectInputStream(
				new BufferedInputStream(new GZIPInputStream(new FileInputStream(filename))))) {
			scoreMatrix = (double[][]) dis.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(File file) {
		try (ObjectInputStream dis = new ObjectInputStream(
				new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))))) {
			scoreMatrix = (double[][]) dis.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(InputStream inputStream) {
		try (ObjectInputStream dis = new ObjectInputStream(
				new BufferedInputStream(new GZIPInputStream(inputStream)))) {
			scoreMatrix = (double[][]) dis.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
