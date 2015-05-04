package io.vertx.webchat.auth.hash;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

public class HashInfo {
	private String hashAlgorithm;
	private int hashIterations;
	private boolean hexEncoded;

	/**
	 * This class provides information about a {@link SimpleHash} that may be
	 * used in combination
	 * with a {@link HashedCredentialsMatcher}.
	 * 
	 * @param algorithm The algorithm-name
	 * @param iterations The amount of hashing-iterations used in a
	 *            {@link SimpleHash}
	 * @param hexEncoded True, if the algorithm uses hex-encoding, otherwise
	 *            base64
	 */
	public HashInfo(String algorithm, int iterations, boolean hexEncoded) {
		this.setAlgorithmName(algorithm);
		this.setIterations(iterations);
		this.setHexEncoded(hexEncoded);
	}

	/**
	 * @return The algorithm-name
	 */
	public String getAlgorithmName() {
		return hashAlgorithm;
	}

	/**
	 * @param hashAlgorithm The algorithm-name
	 */
	public void setAlgorithmName(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	/**
	 * @return The amount of hashing-iterations used in a {@link SimpleHash}
	 */
	public int getIterations() {
		return hashIterations;
	}

	/**
	 * @param hashIterations The amount of hashing-iterations used in a
	 *            {@link SimpleHash}
	 */
	public void setIterations(int hashIterations) {
		this.hashIterations = hashIterations;
	}

	/**
	 * @return True, if the algorithm uses hex-encoding, otherwise base64
	 */
	public boolean isHexEncoded() {
		return hexEncoded;
	}

	/**
	 * @param hexEncoded True, if the algorithm uses hex-encoding, otherwise
	 *            base64
	 */
	public void setHexEncoded(boolean hexEncoded) {
		this.hexEncoded = hexEncoded;
	}

	public String toString() {
		return "algorithm:" + getAlgorithmName() + "; iterations:" + getIterations() + "; isHex:" + isHexEncoded();
	}
}
