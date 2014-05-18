/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: objectarray.h
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb and jin/libuvc folder may have a different license, see the respective files.
*/

#ifndef OBJECTARRAY_H_
#define OBJECTARRAY_H_

template <class T>
class ObjectArray {
private:
	T *m_elements;
	const int min_size;
	int m_max_size;
	int m_size;
public:
	ObjectArray(int initial_capacity = 2)
		: m_elements(new T[initial_capacity]),
		  m_max_size(initial_capacity),
		  m_size(0),
		  min_size(initial_capacity) {
	}

	~ObjectArray() { SAFE_DELETE_ARRAY(m_elements); }
	void size(int new_size) {
		if (new_size != capacity()) {
			T *new_elements = new T[new_size];
			if (!new_elements) {
				LOGE("out of memory");
			}
			int n = (new_size < capacity()) ? new_size : capacity();
			for (int i = 0; i < n; i++) {
				new_elements[i] = m_elements[i];
			}
			SAFE_DELETE_ARRAY(m_elements);
			m_elements = new_elements;
			m_max_size = new_size;
			m_size = (m_size < new_size) ? m_size : new_size;
		}
	}

	inline int size() const { return m_size; }
	inline int capacity() const { return m_max_size; }
	inline T &operator[](int index) { return m_elements[index]; }
	inline const T &operator[](int index) const { return m_elements[index]; }
	int put(T object) {
		if (object) {
			if (size() >= capacity()) {
				size(capacity() ? capacity() * 2 : 2);
			}
			m_elements[m_size++] = object;
		}
		return m_size;
	}
	T remove(int index) {
		T obj = m_elements[index];
		for (int i = index; i < m_size - 1; i++) {
			m_elements[i] = m_elements[i+1];
		}
		m_size--;
		return obj;
	}
	void removeObject(T object) {
		for (int i = 0; i < size(); i++) {
			if (m_elements[i] == object) {
				remove(i);
				break;
			}
		}
	}

	int getIndex(const T object) {
		int result = -1;
		for (int i = 0; i < size(); i++) {
			if (m_elements[i] == object) {
				result = i;
				break;
			}
		}
		return result;
	}

	void clear() {
		size(min_size);
		m_size = 0;
	}
};

#endif	// OBJECTARRAY_H_