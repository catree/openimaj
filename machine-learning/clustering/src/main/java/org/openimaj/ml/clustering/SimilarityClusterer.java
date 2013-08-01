package org.openimaj.ml.clustering;

import ch.akuhn.matrix.SparseMatrix;


/**
 * A {@link SimilarityClusterer} clusters data that can be represented as a similarity
 * matrix. Specifically these clusterers only need to know (in some sense) how similar two 
 * things are and nothing else
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <CLUSTERS> 
 *
 */
public interface SimilarityClusterer<CLUSTERS extends IndexClusters> extends SparseMatrixClusterer<CLUSTERS> {
	/**
	 * @param sim the similarity matrix
	 * @return the clusters
	 */
	public CLUSTERS clusterSimilarity(SparseMatrix sim);
}
