/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.jvm.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.isSuspendFunctionType
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.unpackFunctionLiteral
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.jvm.annotations.findSynchronizedAnnotation
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm
import org.jetbrains.kotlin.resolve.source.getPsi

object SynchronizedApplicabilityChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (descriptor is FunctionDescriptor) {
            val annotation = descriptor.findSynchronizedAnnotation()
            if (annotation != null) {
                val reportOn = DescriptorToSourceUtils.getSourceFromAnnotation(annotation) ?: declaration
                if (descriptor.isInline) {
                    context.trace.report(ErrorsJvm.SYNCHRONIZED_ON_INLINE.on(reportOn))
                } else if (descriptor.isSuspend) {
                    context.trace.report(ErrorsJvm.SYNCHRONIZED_ON_SUSPEND.on(reportOn))
                }
            }
        }
    }
}

object SynchronizedCallChecked : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        for ((param, arg) in resolvedCall.valueArguments) {
            if (!param.type.isSuspendFunctionType) continue

            val annotation = arg.arguments.mapNotNull {
                val literal = it.getArgumentExpression()?.unpackFunctionLiteral()?.functionLiteral ?: return@mapNotNull null
                context.trace.bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, literal]?.findSynchronizedAnnotation()
            }.firstOrNull()
            if (annotation != null) {
                context.trace.report(ErrorsJvm.SYNCHRONIZED_ON_SUSPEND.on(annotation.source.getPsi() ?: reportOn))
            }
        }
    }
}